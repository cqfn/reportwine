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

import com.haulmont.yarg.structure.BandData;
import java.util.List;
import org.cqfn.reportwine.exceptions.BaseException;
import org.cqfn.reportwine.model.Array;
import org.cqfn.reportwine.model.Pair;
import org.cqfn.reportwine.model.Text;
import org.cqfn.reportwine.model.Value;

/**
 * Converter from the intermediate representation (IR) to YARG {@link BandData} that contains
 * bindings for the document template.
 *
 * @since 0.1
 */
public class IrToYargConverter {
    /**
     * The intermediate structure of the document data binding.
     */
    private final Pair structure;

    /**
     * Constructor.
     * @param structure The intermediate representation of data to be parsed
     */
    public IrToYargConverter(final Pair structure) {
        this.structure = structure;
    }

    /**
     * Converts the IR into YARG bindings for variable replacement in docx template.
     * @return The {@link BandData} object with YARG bindings
     * @throws BaseException If an error occurs during IR parsing
     */
    public BandData convert() throws BaseException {
        final BandData result = new BandData(this.structure.getKey());
        this.processPair(this.structure, result);
        return result;
    }

    /**
     * Processes the IR object.
     * @param pair The IR pair element
     * @param result The result YARG bindings
     * @throws BaseException If an error occurs during IR parsing
     */
    private void processPair(final Pair pair, final BandData result) throws BaseException {
        final Value value = pair.getValue();
        if (value instanceof Array) {
            final Array array = (Array) value;
            if (array.isTextArray()) {
                result.addData(pair.getKey(), IrToYargConverter.generateList(array.getValue()));
            } else if (array.isTable()) {
                this.processArrayAsTable(pair.getKey(), array, result);
            } else {
                for (final Value item : array.getValue()) {
                    final Pair nested = (Pair) item;
                    this.processPair(nested, result);
                }
            }
        } else if (value instanceof Pair) {
            final Pair nested = (Pair) value;
            final BandData band = new BandData(pair.getKey(), result);
            result.addChild(band);
            this.processPair(nested, band);
        } else if (value instanceof Text) {
            result.addData(pair.getKey(), ((Text) value).getValue());
        }
    }

    /**
     * Processes the IR array that specifies a table.
     * @param name The name of the tag for YARG bindings
     * @param array The IR array element
     * @param result The result YARG bindings
     * @throws BaseException If an error occurs during IR parsing
     */
    private void processArrayAsTable(
        final String name, final Array array, final BandData result) throws BaseException {
        for (final Value item : array.getValue()) {
            final Array row = (Array) item;
            final BandData band = new BandData(name, result);
            result.addChild(band);
            for (final Value value : row.getValue()) {
                if (value instanceof Pair) {
                    this.processPair((Pair) value, band);
                }
            }
        }
    }

    /**
     * Generates multiline list of values.
     * @param values The values to be added into the list
     * @return The list as string with newline characters
     */
    private static String generateList(final List<Value> values) {
        final StringBuilder builder = new StringBuilder();
        int idx = 1;
        for (final Value item : values) {
            builder.append(idx).append(". ").append(((Text) item).getValue()).append('\n');
            idx += 1;
        }
        return builder.toString();
    }
}