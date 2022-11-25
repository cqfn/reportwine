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

package org.cqfn.reportwine.model;

import org.cqfn.reportwine.utils.JsExecutor;

/**
 * Handler of {@link Code} values in the intermediate representation (IR) model
 * that runs {@link JsExecutor} for each script to retrieve values.
 *
 * @since 0.1
 */
public class CodeHandler {
    /**
     * The root node of the model.
     */
    private final Pair root;

    /**
     * Constructor.
     * @param root The root node of the model
     */
    public CodeHandler(final Pair root) {
        this.root = root;
    }

    /**
     * Traverses the model to find all code entities and execute scripts
     * in their values.
     * @return Model with all code entities replaced with the calculated values
     *  or an initial model
     */
    public Pair process() {
        Pair result = this.root;
        final Value value = this.root.getValue();
        if (value instanceof Array) {
            final Array array = (Array) value;
            if (array.isPairArray()) {
                for (final Value pair : array.getValues()) {
                    result = CodeHandler.processPair((Pair) pair, result);
                }
            }
        } else if (value instanceof Pair) {
            result = CodeHandler.processPair((Pair) value, result);
        }
        return result;
    }

    /**
     * If a pair has a code value, executes this script to calculate a new value.
     * @param pair The pair to be processed
     * @param model The current state of the model
     * @return Model with changes
     */
    private static Pair processPair(final Pair pair, final Pair model) {
        Pair result = model;
        final Value value = pair.getValue();
        if (value instanceof Code) {
            final Code code = (Code) value;
            final JsExecutor executor = new JsExecutor(model);
            result = executor.exec(pair.getKey(), code.getValue());
        }
        return result;
    }
}
