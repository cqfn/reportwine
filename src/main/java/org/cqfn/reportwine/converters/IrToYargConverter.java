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
import org.cqfn.reportwine.exceptions.BaseException;
import org.cqfn.reportwine.exceptions.ExpectedArrayList;
import org.cqfn.reportwine.exceptions.ExpectedPairArray;
import org.cqfn.reportwine.exceptions.ExpectedTextArray;
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
        this.processPair(this.structure, result, true);
        return result;
    }

    /**
     * Processes the IR Pair object.
     * @param pair The IR pair element
     * @param parent The parent YARG bindings
     * @param root Identifies if the current parent bindings is the root one
     * @throws BaseException If an error occurs during IR arrays parsing
     */
    private void processPair(final Pair pair, final BandData parent, final boolean root)
        throws BaseException {
        final Value value = pair.getValue();
        if (value instanceof Array) {
            final Array array = (Array) value;
            if (array.isTextArray()) {
                IrToYargConverter.processTextArray(pair.getKey(), array, parent);
            } else if (array.isArrayList()) {
                this.processArrayAsTable(pair.getKey(), array, parent);
            } else if (array.isPairArray()) {
                this.processNestedPairs(pair, parent, root);
            } else {
                IrToYargConverter.generateArrayException(array.getValue(0));
            }
        } else if (value instanceof Pair) {
            final Pair nested = (Pair) value;
            final BandData band = new BandData(pair.getKey(), parent);
            parent.addChild(band);
            this.processPair(nested, band, root);
        } else if (value instanceof Text) {
            parent.addData(pair.getKey(), ((Text) value).getValue());
        }
    }

    /**
     * Generates multiline list of values for a binding.
     * @param name The name of the tag for YARG binding
     * @param array The IR array element
     * @param result The result YARG bindings
     */
    private static void processTextArray(
        final String name, final Array array, final BandData result) {
        final StringBuilder builder = new StringBuilder();
        int idx = 1;
        for (final Value item : array.getValues()) {
            builder.append(((Text) item).getValue());
            if (idx != array.size()) {
                builder.append('\n');
            }
            idx += 1;
        }
        result.addData(name, builder.toString());
    }

    /**
     * Processes the IR Array of Pairs.
     * @param pair The IR pair element
     * @param parent The parent YARG bindings
     * @param root Identifies if the current parent bindings is the root one
     * @throws BaseException If an error occurs during IR parsing
     */
    private void processNestedPairs(final Pair pair, final BandData parent, final boolean root)
        throws BaseException {
        final Value value = pair.getValue();
        final Array array = (Array) value;
        BandData nested = parent;
        if (!root) {
            nested = new BandData(pair.getKey(), parent);
            parent.addChild(nested);
        }
        for (final Value item : array.getValues()) {
            final Pair child = (Pair) item;
            this.processPair(child, nested, false);
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
        for (final Value item : array.getValues()) {
            final Array row = (Array) item;
            final BandData band = new BandData(name, result);
            result.addChild(band);
            for (final Value value : row.getValues()) {
                if (value instanceof Pair) {
                    this.processPair((Pair) value, band, false);
                }
            }
        }
    }

    /**
     * Generates specific exceptions for arrays misuse cases.
     * @param child The first element of the array that identifies
     *  the type of the array
     * @throws ExpectedTextArray If a text array is expected, but other objects
     *  are found in the array
     * @throws ExpectedPairArray If an array of pairs is expected, but other objects
     *  are found in the list
     * @throws ExpectedArrayList If an array list is expected, but other objects
     *  are found in the list
     */
    private static void generateArrayException(final Value child)
        throws ExpectedTextArray, ExpectedPairArray, ExpectedArrayList {
        if (child instanceof Text) {
            throw new ExpectedTextArray(child.toString());
        }
        if (child instanceof Pair) {
            throw new ExpectedPairArray(child.toString());
        }
        if (child instanceof Array) {
            throw new ExpectedArrayList(child.toString());
        }
    }
}
