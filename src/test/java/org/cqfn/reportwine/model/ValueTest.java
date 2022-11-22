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
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link Value} classes.
 *
 * @since 0.1
 */
class ValueTest {
    /**
     * Test the equality of Text objects.
     */
    @Test
    void testTextEquality() {
        final Text first = new Text("toBeCompared");
        final Text second = new Text("toBeCompared");
        Assertions.assertEquals(first, second);
        final Text third = new Text("tobecompared");
        Assertions.assertNotEquals(first, third);
    }

    /**
     * Test the equality of Code objects.
     */
    @Test
    void testCodeEquality() {
        final Code first = new Code("$ return milestones[current_phase - 1].objectives;");
        final Code second = new Code("$ return milestones[current_phase - 1].objectives;");
        Assertions.assertEquals(first, second);
        final Code third = new Code("$ return milestones[current_phase].objectives;");
        Assertions.assertNotEquals(first, third);
    }

    /**
     * Test the equality of Array objects.
     */
    @Test
    void testArrayEquality() {
        final List<Value> values = new LinkedList<>();
        values.add(new Text("1"));
        values.add(new Text("2"));
        values.add(new Text("3"));
        final Array first = new Array(values);
        final List<Value> similar = new LinkedList<>();
        similar.add(new Text("1"));
        similar.add(new Text("2"));
        similar.add(new Text("3"));
        final Array second = new Array(similar);
        Assertions.assertEquals(first, second);
        final List<Value> different = new LinkedList<>();
        different.add(new Text("1"));
        final Array third = new Array(different);
        Assertions.assertNotEquals(first, third);
    }

    /**
     * Test the equality of Array objects.
     */
    @Test
    void testPairEquality() {
        final Pair first = new Pair("one");
        first.setValue(
            new Array(
                Arrays.asList(
                    new Text("text"),
                    new Text("data")
                )
            )
        );
        final Pair second = new Pair("one");
        second.setValue(
            new Array(
                Arrays.asList(
                    new Text("text"),
                    new Text("data")
                )
            )
        );
        Assertions.assertEquals(first, second);
        final Pair third = new Pair("one");
        third.setValue(
            new Array(
                Arrays.asList(
                    new Text("other text"),
                    new Text("other data")
                )
            )
        );
        Assertions.assertNotEquals(first, third);
        final Pair fourth = new Pair("two");
        fourth.setValue(
            new Array(
                Arrays.asList(
                    new Text("text"),
                    new Text("data")
                )
            )
        );
        Assertions.assertNotEquals(first, fourth);
    }
}
