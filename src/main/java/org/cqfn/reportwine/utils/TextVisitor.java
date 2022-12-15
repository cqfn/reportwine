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

package org.cqfn.reportwine.utils;

import com.haulmont.yarg.formatters.impl.AbstractFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.dml.CTRegularTextRun;
import org.docx4j.dml.CTTableCell;
import org.docx4j.dml.CTTextBody;
import org.docx4j.dml.CTTextLineBreak;
import org.docx4j.dml.CTTextParagraph;

/**
 * Visitor of Docx4j objects that contain text.
 *
 * @since 0.1
 */
public final class TextVisitor extends TraversalUtil.CallbackImpl {
    /**
     * The mappings of text paragraphs with the variable names.
     */
    private final Map<CTTextParagraph, String> mappings;

    /**
     * Constructor.
     */
    public TextVisitor() {
        this.mappings = new HashMap<>();
    }

    /**
     * Returns collected mappings of text paragraphs with the variable names.
     * @return The mappings
     */
    public Map<CTTextParagraph, String> getMappings() {
        return this.mappings;
    }

    @Override
    public List<Object> apply(final Object obj) {
        CTTextBody body = null;
        if (obj instanceof org.pptx4j.pml.Shape) {
            body = ((org.pptx4j.pml.Shape) obj).getTxBody();
        } else if (obj instanceof CTTableCell) {
            body = ((CTTableCell) obj).getTxBody();
        }
        if (body != null) {
            this.processTextBody(body);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean shouldTraverse(final Object obj) {
        return true;
    }

    @Override
    public void walkJAXBElements(final Object parent) {
        final List<Object> children = this.getChildren(parent);
        if (children != null) {
            for (final Object child : children) {
                final Object obj = XmlUtils.unwrap(child);
                this.apply(obj);
                if (this.shouldTraverse(obj)) {
                    this.walkJAXBElements(obj);
                }
            }
        }
    }

    @Override
    public List<Object> getChildren(final Object obj) {
        final List<Object> result;
        if (obj instanceof org.pptx4j.pml.CTGraphicalObjectFrame) {
            final org.docx4j.dml.Graphic graphic =
                ((org.pptx4j.pml.CTGraphicalObjectFrame) obj).getGraphic();
            if (graphic == null || graphic.getGraphicData() == null) {
                result = null;
            } else {
                result = graphic.getGraphicData().getAny();
            }
        } else {
            result = TraversalUtil.getChildrenImpl(obj);
        }
        return result;
    }

    /**
     * Processes bodies of text paragraphs to get stored data and adds it to mappings, if
     * the found data is a variable, that is, it matches a special pattern.
     * @param body The body of the text paragraph
     */
    private void processTextBody(final CTTextBody body) {
        for (final CTTextParagraph paragraph : body.getP()) {
            final List<Object> params = paragraph.getEGTextRun();
            String value = "";
            if (!params.isEmpty()) {
                value = TextVisitor.concatParams(params);
                if (AbstractFormatter.UNIVERSAL_ALIAS_PATTERN.matcher(value).find()) {
                    this.mappings.put(paragraph, value);
                }
            }
        }
    }

    /**
     * Concatenates parts of text values to collect a full text.
     * @param params Parameters containing text parts
     * @return The full text
     */
    private static String concatParams(final List<Object> params) {
        final StringBuilder builder = new StringBuilder();
        for (final Object value : params) {
            if (value instanceof CTRegularTextRun) {
                final CTRegularTextRun text = (CTRegularTextRun) value;
                builder.append(text.getT());
            } else if (value instanceof CTTextLineBreak) {
                builder.append(System.lineSeparator());
            }
        }
        return builder.toString();
    }
}
