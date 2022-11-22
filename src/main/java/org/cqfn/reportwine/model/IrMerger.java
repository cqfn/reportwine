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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.cqfn.reportwine.exceptions.BaseException;

/**
 * Merger of two intermediate representation (IR) structures into one.
 * Takes a first structure as a base and changes its data overridden in a second structure or
 * adds new data from the second one to the first.
 *
 * @since 0.1
 */
public class IrMerger {
    /**
     * Merges two IR pairs.
     * @param first The first pair with initial information
     * @param second The second pair with additional data or data for replacement
     * @return A new pair
     * @throws BaseException If an error during an IR processing occurs
     */
    public Pair merge(final Pair first, final Pair second) throws BaseException {
        Pair result = null;
        if (first.getKey().equals(second.getKey())) {
            result = new Pair(first.getKey());
            final Value firstval = first.getValue();
            final Value secondval = second.getValue();
            if (firstval instanceof Array || secondval instanceof Array) {
                this.processCasesWithArray(result, firstval, secondval);
            }
            if (firstval instanceof Text && secondval instanceof Text) {
                result.setValue(secondval);
            }
            if (firstval instanceof Pair && secondval instanceof Pair) {
                result.setValue(this.merge((Pair) firstval, (Pair) secondval));
            }
        } else {
            result = first;
        }
        return result;
    }

    /**
     * Merges values if one of them is an array or both are arrays.
     * @param result The pair with a merged value
     * @param first The first value
     * @param second The second value
     * @throws BaseException If an error during an IR processing occurs
     */
    private void processCasesWithArray(final Pair result, final Value first, final Value second)
        throws BaseException {
        if (first instanceof Array && second instanceof Array) {
            final Array array = this.mergeArrays((Array) first, (Array) second);
            result.setValue(array);
        }
        if (first instanceof Array && second instanceof Pair) {
            final Array array = this.mergeArrays(
                (Array) first, new Array(Collections.singletonList(second))
            );
            result.setValue(array);
        }
        if (first instanceof Pair && second instanceof Array) {
            final Array array = this.mergeArrays(
                new Array(Collections.singletonList(first)), (Array) second
            );
            result.setValue(array);
        }
    }

    /**
     * Merges two IR arrays.
     * @param first The first array with initial information
     * @param second The second array with additional data or data for replacement
     * @return A new array
     * @throws BaseException If an error during an IR processing occurs
     */
    private Array mergeArrays(final Array first, final Array second) throws BaseException {
        List<Value> values = new LinkedList<>();
        if (first.isPairArray() && second.isPairArray()) {
            values.addAll(first.getValues());
            for (final Value value : second.getValues()) {
                final Pair pair = (Pair) value;
                final Pair other = IrMerger.findPairByKey(pair.getKey(), values);
                if (other == null) {
                    values.add(pair);
                } else {
                    final int idx = values.indexOf(other);
                    final Pair result = this.merge(other, pair);
                    values.set(idx, result);
                }
            }
        }
        if (first.isTextArray() && second.isTextArray()) {
            values = second.getValues();
        }
        if (first.isArrayList() && second.isArrayList()) {
            values = second.getValues();
        }
        return new Array(values);
    }

    /**
     * Finds a pair with the particular key in the specified list.
     * @param key The key
     * @param values The list of values
     * @return A pair with the specified key if it is found or {@code null} otherwise
     */
    private static Pair findPairByKey(final String key, final List<Value> values) {
        Pair result = null;
        for (final Value value : values) {
            final Pair pair = (Pair) value;
            if (key.equals(pair.getKey())) {
                result = pair;
            }
        }
        return result;
    }
}
