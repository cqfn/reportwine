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
import com.haulmont.yarg.structure.BandData;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.cqfn.reportwine.exceptions.ExpectedScalarException;

/**
 * Converter from the YAML settings to {@link BandData} that contains
 * mappings for the document template.
 *
 * @since 0.1
 */
public class YamlToBandDataConverter {
    /**
     * The YAML object.
     */
    private final YamlMapping yaml;

    /**
     * Constructor.
     * @param yaml The YAML object to be parsed
     */
    public YamlToBandDataConverter(final YamlMapping yaml) {
        this.yaml = yaml;
    }

    /**
     * Converts the YAML file into mappings for variable replacement in docx template.
     * @return The {@link BandData} object with YARG mappings
     * @throws ExpectedScalarException If an error occurs during YAML parsing
     */
    public BandData convert() throws ExpectedScalarException {
        final Set<YamlNode> keys = this.yaml.keys();
        YamlMapping item = this.yaml;
        String root = "";
        if (keys.size() > 1) {
            root = "Doc";
        } else if (keys.size() == 1) {
            root = ((Scalar) keys.stream().findFirst().get()).value();
            item = this.yaml.value(keys.stream().findFirst().get()).asMapping();
        }
        final BandData result = new BandData(root);
        this.processYamlRoot(item, result);
        return result;
    }

    /**
     * Processes the root of the YAML object.
     * @param item The root YAML object
     * @param result The result mapping
     * @throws ExpectedScalarException If an error occurs during YAML parsing
     */
    private void processYamlRoot(final YamlMapping item, final BandData result)
        throws ExpectedScalarException {
        for (final YamlNode key : item.keys()) {
            final YamlNode value = item.value(key);
            switch (value.type()) {
                case SCALAR:
                    result.addData(
                        ((Scalar) key).value(),
                        value.asScalar().value()
                            .replaceAll("\r\n", "")
                            .replaceAll("( )+", " ")
                    );
                    break;
                case MAPPING:
                    this.processYamlMapping(value.asMapping(), ((Scalar) key).value(), result);
                    break;
                case SEQUENCE:
                    this.processYamlSequence(value.asSequence(), ((Scalar) key).value(), result);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Processes the YAML mapping item.
     * @param item The YAML mapping
     * @param name The name of the tag for YARG mapping
     * @param parent The parent YARG mapping object
     * @return The new YARG mapping object
     * @throws ExpectedScalarException If an error occurs during YAML parsing
     */
    private BandData processYamlMapping(
        final YamlMapping item, final String name, final BandData parent)
        throws ExpectedScalarException {
        final BandData result = new BandData(name, parent);
        for (final YamlNode key : item.keys()) {
            final YamlNode value = item.value(key);
            switch (value.type()) {
                case SCALAR:
                    result.addData(((Scalar) key).value(), value.asScalar().value());
                    break;
                case MAPPING:
                    result.addChild(
                        this.processYamlMapping(value.asMapping(), ((Scalar) key).value(), result)
                    );
                    break;
                case SEQUENCE:
                    this.processYamlSequence(value.asSequence(), ((Scalar) key).value(), result);
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    /**
     * Processes the YAML sequence item.
     * @param item The YAML mapping
     * @param name The name of the tag for YARG mapping
     * @param result The result YARG mapping object
     * @throws ExpectedScalarException If an error occurs during YAML parsing
     */
    private void processYamlSequence(
        final YamlSequence item, final String name, final BandData result)
        throws ExpectedScalarException {
        boolean list = false;
        final List<String> values = new ArrayList<>(item.size());
        int idx = 0;
        for (final YamlNode node : item.values()) {
            if (idx == 0) {
                list = node.type().equals(Node.SCALAR);
            } else {
                if (list && !node.type().equals(Node.SCALAR)) {
                    throw new ExpectedScalarException(item.toString());
                }
            }
            if (list) {
                values.add(node.asScalar().value());
            } else {
                result.addChild(this.processYamlMapping(item.yamlMapping(idx), name, result));
            }
            idx += 1;
        }
        if (list) {
            result.addData(name, YamlToBandDataConverter.generateList(values));
        }
    }

    /**
     * Generates multiline list of values.
     * @param values The values to be added into the list
     * @return The list as string with newline characters
     */
    private static String generateList(final List<String> values) {
        final StringBuilder builder = new StringBuilder();
        int idx = 1;
        for (final String item : values) {
            builder.append(idx).append(". ").append(item).append('\n');
            idx += 1;
        }
        return builder.toString();
    }
}
