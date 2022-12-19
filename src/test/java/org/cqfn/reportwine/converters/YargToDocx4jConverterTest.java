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
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link YargToDocx4jConverter} class.
 *
 * @since 0.1
 */
class YargToDocx4jConverterTest {
    /**
     * The "name" literal.
     */
    private static final String NAME = "name";

    /**
     * The "role" literal.
     */
    private static final String ROLE = "role";

    /**
     * Test conversion of BandData with one layer of nested children bands
     * into mappings for Docx4j.
     */
    @Test
    void testConversionOfSingleLayerBands() {
        final BandData project = new BandData("project");
        project.addData(YargToDocx4jConverterTest.NAME, "MyProject");
        project.addData("current_stage", "3");
        final BandData first = new BandData("team", project);
        first.addData(YargToDocx4jConverterTest.NAME, "Ivan");
        first.addData(YargToDocx4jConverterTest.ROLE, "teamleader");
        final BandData second = new BandData("team", project);
        second.addData(YargToDocx4jConverterTest.NAME, "Polina");
        second.addData(YargToDocx4jConverterTest.ROLE, "developer");
        project.addChildren(Arrays.asList(first, second));
        final YargToDocx4jConverter converter = new YargToDocx4jConverter(project);
        final Map<String, String> mappings = converter.convert();
        Assertions.assertEquals(4, mappings.size());
        Assertions.assertNotNull(mappings.get("project.name"));
        Assertions.assertNotNull(mappings.get(YargToDocx4jConverterTest.NAME));
        Assertions.assertNotNull(mappings.get("project.current_stage"));
        Assertions.assertNotNull(mappings.get("current_stage"));
        Assertions.assertEquals("MyProject", mappings.get("project.name"));
        Assertions.assertEquals("MyProject", mappings.get(YargToDocx4jConverterTest.NAME));
        Assertions.assertEquals("3", mappings.get("project.current_stage"));
        Assertions.assertEquals("3", mappings.get("current_stage"));
        final Map<String, List<BandData>> tables = converter.getTables();
        Assertions.assertEquals(2, tables.size());
        final List<BandData> firsttable = tables.get("project.team");
        final List<BandData> sectable = tables.get("team");
        Assertions.assertNotNull(firsttable);
        Assertions.assertNotNull(sectable);
        Assertions.assertEquals(firsttable, sectable);
        Assertions.assertEquals(2, firsttable.size());
        Map<String, Object> data = firsttable.get(0).getData();
        Assertions.assertEquals(2, data.size());
        Assertions.assertEquals("Ivan", data.get(YargToDocx4jConverterTest.NAME));
        Assertions.assertEquals("teamleader", data.get(YargToDocx4jConverterTest.ROLE));
        data = firsttable.get(1).getData();
        Assertions.assertEquals(2, data.size());
        Assertions.assertEquals("Polina", data.get(YargToDocx4jConverterTest.NAME));
        Assertions.assertEquals("developer", data.get(YargToDocx4jConverterTest.ROLE));
    }
}
