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
import java.util.Arrays;
import java.util.List;
import org.cqfn.reportwine.exceptions.BaseException;
import org.cqfn.reportwine.model.Array;
import org.cqfn.reportwine.model.Pair;
import org.cqfn.reportwine.model.Text;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link IrToYargConverter} class.
 *
 * @since 0.1
 */
class IrToYargConverterTest {
    /**
     * Convert an IR array of pairs to YARG bindings.
     */
    @Test
    void convertArrayOfPairs() {
        final Pair first = new Pair("one");
        first.setValue(new Text("1"));
        final Pair second = new Pair("two");
        second.setValue(new Text("2"));
        final Array array = new Array(
            Arrays.asList(
                first,
                second
            )
        );
        final Pair root = new Pair("report");
        root.setValue(array);
        final IrToYargConverter converter = new IrToYargConverter(root);
        boolean oops = false;
        BandData bindings = null;
        try {
            bindings = converter.convert();
        } catch (final BaseException exception) {
            oops = true;
        }
        Assertions.assertFalse(oops);
        Assertions.assertEquals(2, bindings.getData().size());
        Assertions.assertEquals(0, bindings.getChildrenBands().size());
    }

    /**
     * Convert an IR array of arrays to YARG bindings.
     */
    @Test
    void convertArrayOfArrays() {
        final Pair firstdescr = new Pair("description");
        firstdescr.setValue(new Text("First stage"));
        final Pair seconddescr = new Pair("description");
        seconddescr.setValue(new Text("Second stage"));
        final Pair firstdur = new Pair("duration");
        firstdur.setValue(new Text("3"));
        final Pair seconddur = new Pair("duration");
        seconddur.setValue(new Text("4"));
        final Array first = new Array(
            Arrays.asList(
                firstdescr,
                firstdur
            )
        );
        final Array second = new Array(
            Arrays.asList(
                seconddescr,
                seconddur
            )
        );
        final Array table = new Array(
            Arrays.asList(
                first,
                second
            )
        );
        final Pair milestones = new Pair("milestones");
        milestones.setValue(table);
        final IrToYargConverter converter = new IrToYargConverter(milestones);
        boolean oops = false;
        BandData bindings = null;
        try {
            bindings = converter.convert();
        } catch (final BaseException exception) {
            oops = true;
        }
        Assertions.assertFalse(oops);
        Assertions.assertEquals(0, bindings.getData().size());
        Assertions.assertEquals(1, bindings.getChildrenBands().size());
        final List<BandData> tableband = bindings.getChildrenByName("milestones");
        Assertions.assertEquals(2, tableband.size());
    }
}
