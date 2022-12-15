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

package org.cqfn.reportwine.generators;

import com.haulmont.yarg.formatters.impl.AbstractFormatter;
import com.haulmont.yarg.structure.BandData;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import org.apache.commons.lang3.StringUtils;
import org.cqfn.reportwine.utils.ClassFinder;
import org.cqfn.reportwine.utils.TextVisitor;
import org.docx4j.Docx4J;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.dml.CTRegularTextRun;
import org.docx4j.dml.CTTable;
import org.docx4j.dml.CTTableCell;
import org.docx4j.dml.CTTableRow;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.pptx4j.Pptx4jException;

/**
 * Generator of pptx files.
 *
 * @since 0.1
 */
public class PptxGenerator {
    /**
     * The data to be inserted into the variable placeholders in the document template.
     */
    private final Map<String, String> mappings;

    /**
     * The mappings of band names with lists of data to fill existing tables in the template.
     */
    private final Map<String, List<BandData>> tables;

    /**
     * Constructor.
     * @param mappings The mapping for Docx4j variable bindings
     * @param tables The mappings of band names with lists of data to fill tables
     */
    public PptxGenerator(
        final Map<String, String> mappings, final Map<String, List<BandData>> tables) {
        this.mappings = mappings;
        this.tables = tables;
    }

    /**
     * Renders a document with data collected from the YAML file.
     * @param template The template file
     * @param output The file to store the generated document
     * @throws Docx4JException If an error occurs during loading of pptx template
     * @throws Pptx4jException If an error occurs during loading of pptx slides
     */
    public void renderDocument(final File template, final File output)
        throws Docx4JException, Pptx4jException {
        final PresentationMLPackage pptx = PresentationMLPackage.load(template);
        final List<SlidePart> slides = pptx.getMainPresentationPart().getSlideParts();
        for (final SlidePart slide : slides) {
            final TextVisitor visitor = new TextVisitor();
            new TraversalUtil(
                slide.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame(),
                visitor
            );
            final Map<CTTextParagraph, String> candidates = visitor.getMappings();
            for (final Map.Entry<CTTextParagraph, String> item : candidates.entrySet()) {
                this.replaceVariables(item);
            }
            this.processTables(slide);
        }
        Docx4J.save(pptx, output);
    }

    /**
     * Replaces variables in the placeholders of the pptx template with the corresponding data.
     * @param item The text of the paragraph which contains variables to be replaced and its
     *  Docx4j object
     */
    private void replaceVariables(final Map.Entry<CTTextParagraph, String> item) {
        final Matcher matcher = AbstractFormatter.UNIVERSAL_ALIAS_PATTERN.matcher(item.getValue());
        String modified = "";
        while (matcher.find()) {
            final String placeholder = matcher.group();
            final String variable = StringUtils.substringBetween(placeholder, "${", "}");
            final String replacement = this.mappings.get(variable);
            if (replacement != null) {
                if (modified.isEmpty()) {
                    modified = item.getValue();
                }
                modified = modified.replace(placeholder, replacement);
            }
        }
        if (!modified.isEmpty()) {
            final CTTextParagraph paragraph = item.getKey();
            final CTRegularTextRun text =
                (CTRegularTextRun) paragraph.getEGTextRun().get(0);
            text.setT(modified);
            paragraph.getEGTextRun().clear();
            paragraph.getEGTextRun().add(text);
        }
    }

    /**
     * Processes tables from the slide.
     * If there is a map of a table band with data, it fills the table with this data.
     * @param slide The slide from the template
     */
    private void processTables(final SlidePart slide) {
        final ClassFinder finder = new ClassFinder(CTTable.class);
        new TraversalUtil(
            slide.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame(),
            finder
        );
        for (final Object obj : finder.getObjects()) {
            final CTTable table = (CTTable) obj;
            final CTTableRow head = table.getTr().get(0);
            final CTTableRow row = table.getTr().get(1);
            final CTTableCell cell = head.getTc().get(0);
            final List<Object> parts = cell.getTxBody().getP().get(0).getEGTextRun();
            final StringBuilder builder = new StringBuilder();
            for (final Object part: parts) {
                final CTRegularTextRun text = (CTRegularTextRun) part;
                builder.append(text.getT());
            }
            final String value = builder.toString();
            final String band = StringUtils.substringBetween(value, "##band=", " ");
            List<BandData> data = null;
            if (band != null) {
                data = this.tables.get(band);
            }
            if (data != null) {
                for (final BandData array : data) {
                    PptxGenerator.fillTable(table, row, array);
                }
                final CTRegularTextRun text = (CTRegularTextRun) parts.get(0);
                text.setT(
                    value.substring(String.format("##band=%s ", band).length())
                );
                parts.clear();
                parts.add(text);
            }
        }
    }

    /**
     * Fills the table from the slide with data.
     * @param table The table to be modified
     * @param row The row with variable placeholders
     * @param data The data to be inserted
     */
    private static void fillTable(final CTTable table, final CTTableRow row, final BandData data) {
        final CTTableRow cloned = XmlUtils.deepCopy(row);
        final Map<String, String> map = new HashMap<>();
        final Map<String,  Object> items = data.getData();
        for (final Map.Entry<String, Object> item : items.entrySet()) {
            final Object value = item.getValue();
            if (value instanceof String) {
                map.put(item.getKey(), value.toString());
            }
        }
        for (final CTTableCell cell : cloned.getTc()) {
            final List<Object> params = cell.getTxBody().getP().get(0).getEGTextRun();
            if (!params.isEmpty()) {
                final CTRegularTextRun text =
                    (CTRegularTextRun) params.get(0);
                final String value = text.getT();
                final String band = StringUtils.substringBetween(value, "${", "}");
                final String replacement = map.get(band);
                if (replacement != null) {
                    text.setT(replacement);
                }
            }
        }
        table.getTr().add(cloned);
        table.getTr().remove(row);
    }
}
