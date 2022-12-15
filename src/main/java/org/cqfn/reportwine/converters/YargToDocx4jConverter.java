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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cqfn.reportwine.exceptions.BaseException;

/**
 * Converter from the YARG {@link BandData} representation to mappings
 * required for Docx4j variable replacements.
 *
 * @since 0.1
 */
public class YargToDocx4jConverter {
    /**
     * The {@link BandData} root object with YARG bindings.
     */
    private final BandData root;

    /**
     * The mappings of band names with lists of data to fill tables.
     */
    private final Map<String, List<BandData>> tables;

    /**
     * The result Docx4j bindings.
     */
    private final Map<String, String> mappings;

    /**
     * Constructor.
     * @param root The root object with YARG data bindings
     */
    public YargToDocx4jConverter(final BandData root) {
        this.root = root;
        this.tables = new HashMap<>();
        this.mappings = new HashMap<>();
    }

    /**
     * Converts YARG objects into Docx4j bindings for variable replacement in pptx template.
     * @return The mapping for Docx4j variable bindings
     * @throws BaseException If an error occurs during IR parsing
     */
    public Map<String, String> convert() throws BaseException {
        this.processBandData(this.root);
        this.processBandChildren(this.root);
        return this.mappings;
    }

    /**
     * Returns the mapping of band (table) names with data to fill tables.
     * @return Mappings of band names with lists of data
     */
    public Map<String, List<BandData>> getTables() {
        return this.tables;
    }

    /**
     * Processes child bands of the BandData object to collect data for tables.
     * @param parent The parent BandData to be processed
     */
    private void processBandChildren(final BandData parent) {
        final Map<String, List<BandData>> map = parent.getChildrenBands();
        for (final Map.Entry<String, List<BandData>> child : map.entrySet()) {
            if (child.getValue().size() > 1) {
                final String tag;
                if (parent.getParentBand() == null) {
                    tag = child.getKey();
                } else {
                    tag = String.format("%s.%s", parent.getName(), child.getKey());
                }
                this.tables.put(
                    tag,
                    child.getValue()
                );
                this.tables.put(
                    String.format(
                        "%s.%s", YargToDocx4jConverter.getFullName(parent), child.getKey()
                    ),
                    child.getValue()
                );
            } else {
                final BandData band = child.getValue().get(0);
                this.processBandData(band);
                this.processBandChildren(band);
            }
        }
    }

    /**
     * Processes the BandData object to collect mappings.
     * @param band The BandData to be processed
     */
    private void processBandData(final BandData band) {
        for (final Map.Entry<String, Object> data : band.getData().entrySet()) {
            final Object value = data.getValue();
            final String prefix;
            if (band.getParentBand() == null) {
                prefix = "";
            } else {
                prefix = String.format("%s.", band.getName());
            }
            if (value instanceof String) {
                this.mappings.put(
                    prefix.concat(data.getKey()),
                    value.toString()
                );
                this.mappings.put(
                    String.format(
                        "%s.%s", YargToDocx4jConverter.getFullName(band), data.getKey()
                    ),
                    value.toString()
                );
            }
        }
    }

    /**
     * Composes a full name of the mapping tag in the format ${parent.nested1.nested2...nestedN},
     * where N is the depth of nesting YAML data.
     * @param band The BandData to be processed
     * @return The full name of the tag
     */
    private static String getFullName(final BandData band) {
        String full = band.getName();
        for (BandData parent = band.getParentBand(); parent != null;
            parent = parent.getParentBand()) {
            full = String.format("%s.%s", parent.getName(), full);
        }
        return full;
    }
}
