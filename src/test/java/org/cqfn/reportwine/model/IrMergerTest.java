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

import java.util.Arrays;
import java.util.Collections;
import org.cqfn.reportwine.exceptions.BaseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link IrMerger} class.
 *
 * @since 0.1
 */
class IrMergerTest {
    /**
     * Test adding a new pair from the second IR structure to the first one.
     */
    @Test
    void addNewPairToInitialStructure() {
        final Pair initial = new Pair("project");
        initial.setValue(
            new Array(
                Arrays.asList(
                    new Pair("start", new Text("2018")),
                    new Pair("end", new Text("2020"))
                )
            )
        );
        final Pair second = new Pair("project");
        second.setValue(
            new Array(
                Collections.singletonList(
                    new Pair("mid", new Text("2019"))
                )
            )
        );
        final IrMerger merger = new IrMerger();
        boolean oops = false;
        Pair result = null;
        try {
            result = merger.merge(initial, second);
        } catch (final BaseException exception) {
            oops = true;
        }
        Assertions.assertFalse(oops);
        Assertions.assertNotNull(result);
        Value value = result.getValue();
        Assertions.assertTrue(value instanceof Array);
        final Array array = (Array) value;
        Assertions.assertEquals(3, array.size());
        value = array.getValue(2);
        Assertions.assertTrue(value instanceof Pair);
        final Pair pair = (Pair) value;
        Assertions.assertEquals("mid", pair.getKey());
        final Value date = pair.getValue();
        Assertions.assertTrue(date instanceof Text);
        Assertions.assertEquals("2019", ((Text) date).getValue());
    }

    /**
     * Test replacing of a text value in the initial IR pair with the data
     * from the second IR structure.
     */
    @Test
    void replaceTextValueInInitialStructure() {
        final Pair initial = new Pair("report");
        initial.setValue(
            new Array(
                Arrays.asList(
                    new Pair("start_date", new Text("2021")),
                    new Pair("end_date", new Text("2023"))
                )
            )
        );
        final Pair second = new Pair("report");
        second.setValue(
            new Array(
                Collections.singletonList(
                    new Pair("end_date", new Text("2022"))
                )
            )
        );
        final IrMerger merger = new IrMerger();
        boolean oops = false;
        Pair result = null;
        try {
            result = merger.merge(initial, second);
        } catch (final BaseException exception) {
            oops = true;
        }
        Assertions.assertFalse(oops);
        Assertions.assertNotNull(result);
        Value value = result.getValue();
        Assertions.assertTrue(value instanceof Array);
        final Array array = (Array) value;
        Assertions.assertEquals(2, array.size());
        value = array.getValue(0);
        Assertions.assertTrue(value instanceof Pair);
        Pair pair = (Pair) value;
        Assertions.assertEquals("start_date", pair.getKey());
        Value date = pair.getValue();
        Assertions.assertTrue(date instanceof Text);
        Assertions.assertEquals("2021", ((Text) date).getValue());
        value = array.getValue(1);
        Assertions.assertTrue(value instanceof Pair);
        pair = (Pair) value;
        Assertions.assertEquals("end_date", pair.getKey());
        date = pair.getValue();
        Assertions.assertTrue(date instanceof Text);
        Assertions.assertEquals("2022", ((Text) date).getValue());
    }

    /**
     * Test replacing of a text value in the initial IR pair with the data
     * from the second IR structure.
     */
    @Test
    void replaceArrayValueInInitialStructure() {
        final Pair initial = new Pair("doc");
        initial.setValue(
            new Array(
                Arrays.asList(
                    new Text("1"),
                    new Text("2"),
                    new Text("3")
                )
            )
        );
        final Pair second = new Pair("doc");
        second.setValue(
            new Array(
                Arrays.asList(
                    new Text("111"),
                    new Text("222"),
                    new Text("333")
                )
            )
        );
        final IrMerger merger = new IrMerger();
        boolean oops = false;
        Pair result = null;
        try {
            result = merger.merge(initial, second);
        } catch (final BaseException exception) {
            oops = true;
        }
        Assertions.assertFalse(oops);
        Assertions.assertNotNull(result);
        final Value value = result.getValue();
        Assertions.assertTrue(value instanceof Array);
        final Array array = (Array) value;
        Assertions.assertEquals(3, array.size());
        final Value one = array.getValue(0);
        Assertions.assertTrue(one instanceof Text);
        Assertions.assertEquals("111", ((Text) one).getValue());
        final Value two = array.getValue(1);
        Assertions.assertTrue(two instanceof Text);
        Assertions.assertEquals("222", ((Text) two).getValue());
        final Value three = array.getValue(2);
        Assertions.assertTrue(three instanceof Text);
        Assertions.assertEquals("333", ((Text) three).getValue());
    }
}
