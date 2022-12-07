/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022 Polina Volkhontseva
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.cqfn.reportwine.converters;

import com.amihaiemil.eoyaml.Node;
import com.amihaiemil.eoyaml.Scalar;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlNode;
import com.amihaiemil.eoyaml.YamlSequence;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.cqfn.reportwine.exceptions.BaseException;
import org.cqfn.reportwine.exceptions.ExpectedScalarException;
import org.cqfn.reportwine.exceptions.UnsupportedYamlFormat;
import org.cqfn.reportwine.model.Array;
import org.cqfn.reportwine.model.Code;
import org.cqfn.reportwine.model.Pair;
import org.cqfn.reportwine.model.Text;
import org.cqfn.reportwine.model.Value;

/**
 * Converter from the YAML settings to the intermediate representation
 * that contains bindings for the document template.
 *
 * @since 0.1
 */
public class YamlToIrConverter {
    /**
     * The YAML object.
     */
    private final YamlMapping yaml;

    /**
     * Constructor.
     * @param yaml The YAML object to be parsed
     */
    public YamlToIrConverter(final YamlMapping yaml) {
        this.yaml = yaml;
    }

    /**
     * Converts the YAML file into internal {@link Pair} structure.
     * @return The {@link Pair}  with data binding
     * @throws BaseException If an error occurs during YAML parsing
     */
    public Pair convert() throws BaseException {
        final Set<YamlNode> keys = this.yaml.keys();
        YamlMapping mapping = this.yaml;
        String root = "";
        if (keys.size() > 1) {
            root = "Doc";
        } else if (keys.size() == 1) {
            root = ((Scalar) keys.stream().findFirst().get()).value();
            final YamlNode node = this.yaml.value(keys.stream().findFirst().get());
            if (!node.type().equals(Node.MAPPING)) {
                throw UnsupportedYamlFormat.INSTANCE;
            }
            mapping = node.asMapping();
        }
        final Pair item = new Pair(root);
        item.setValue(this.processYamlMapping(mapping));
        return item;
    }

    /**
     * Processes the root of the YAML mapping.
     * @param mapping The root YAML mappping
     * @return The value for the binding pair
     * @throws BaseException If an error occurs during YAML parsing
     */
    private Value processYamlMapping(final YamlMapping mapping)
        throws BaseException {
        final List<Value> values = new LinkedList<>();
        for (final YamlNode key : mapping.keys()) {
            final YamlNode value = mapping.value(key);
            final Pair object = new Pair(((Scalar) key).value());
            switch (value.type()) {
                case SCALAR:
                    object.setValue(
                        YamlToIrConverter.processYamlScalar(
                            value.asScalar().value(),
                            value.asScalar().toString()
                        )
                    );
                    break;
                case MAPPING:
                    object.setValue(this.processYamlMapping(value.asMapping()));
                    break;
                case SEQUENCE:
                    this.processYamlSequence(value.asSequence(), object);
                    break;
                default:
                    break;
            }
            values.add(object);
        }
        final Value result;
        if (values.size() > 1) {
            result = new Array(values);
        } else if (values.size() == 1) {
            result = values.get(0);
        } else {
            throw UnsupportedYamlFormat.INSTANCE;
        }
        return result;
    }

    /**
     * Processes the YAML sequence.
     * @param seq The YAML sequence
     * @param item The binding pair
     * @throws BaseException If an error occurs during YAML parsing
     */
    private void processYamlSequence(
        final YamlSequence seq, final Pair item)
        throws BaseException {
        boolean list = false;
        final List<Value> values = new ArrayList<>(seq.size());
        int idx = 0;
        for (final YamlNode node : seq.values()) {
            if (idx == 0) {
                list = node.type().equals(Node.SCALAR);
            } else {
                if (list && !node.type().equals(Node.SCALAR)) {
                    throw new ExpectedScalarException(seq.toString());
                }
            }
            if (list) {
                values.add(
                    YamlToIrConverter.processYamlScalar(
                        node.asScalar().value(),
                        node.asScalar().toString()
                    )
                );
            } else {
                final Value nested = this.processYamlMapping(seq.yamlMapping(idx));
                if (nested instanceof Pair) {
                    final Array array = new Array(
                        Collections.singletonList(nested)
                    );
                    values.add(array);
                } else {
                    values.add(nested);
                }
            }
            idx += 1;
        }
        item.setValue(new Array(values));
    }

    /**
     * Processes the YAML scalar to choose the value type and prepare value.
     * @param scalar The scalar value
     * @param yaml The full YAML scalar with special symbols and indentation
     * @return The {@link Text} or {@link Code} value
     */
    private static Value processYamlScalar(final String scalar, final String yaml) {
        final Value value;
        final String delimiter;
        if (yaml.startsWith("---\r\n|") || yaml.startsWith("---\n|")) {
            delimiter = " ";
        } else {
            delimiter = "";
        }
        if (scalar.charAt(0) == '$') {
            value = new Code(scalar.replaceFirst("\\$", ""));
        } else {
            String text = scalar
                .replaceAll("\r\n", delimiter)
                .replaceAll("\n", delimiter)
                .replaceAll("( )+", " ");
            if (text.charAt(0) == ' ') {
                text = text.replaceFirst("( )+", "");
            }
            if (text.charAt(text.length() - 1) == ' ' && " ".equals(delimiter)) {
                text = text.substring(0, text.length() - 1);
            }
            value = new Text(text);
        }
        return value;
    }
}
