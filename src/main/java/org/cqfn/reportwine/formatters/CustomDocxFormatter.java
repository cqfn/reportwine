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

package org.cqfn.reportwine.formatters;

import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.formatters.impl.DocxFormatter;
import javax.xml.bind.JAXBException;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.toc.TocException;
import org.docx4j.toc.TocFinder;
import org.docx4j.toc.TocGenerator;
import org.docx4j.wml.Body;
import org.docx4j.wml.Document;
import org.docx4j.wml.SdtBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom implementation of {@link DocxFormatter} that replaces newline characters with tags
 * supported by docx documents.
 *
 * @since 0.1
 */
public final class CustomDocxFormatter extends DocxFormatter {
    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CustomDocxFormatter.class);

    /**
     * The newline replacement character.
     */
    private static final String NEWLINE_CHAR = "¶";

    /**
     * Identifies if the document has newline characters.
     */
    private boolean newlines;

    /**
     * Constructor that extends superclass.
     * @param input The formatter factory input
     */
    public CustomDocxFormatter(final FormatterFactoryInput input) {
        super(input);
        this.newlines = false;
    }

    @Override
    public void updateTableOfContents() {
        try {
            final MainDocumentPart part = wordprocessingMLPackage.getMainDocumentPart();
            if (this.newlines) {
                CustomDocxFormatter.replaceNewLines(part);
            }
            final Document document;
            try {
                document = part.getContents();
            } catch (final Docx4JException exception) {
                throw new IllegalStateException(exception);
            }
            final Body body =  document.getBody();
            final TocFinder finder = new TocFinder();
            new TraversalUtil(body.getContent(), finder);
            final SdtBlock sdt = finder.getTocSDT();
            if (sdt != null) {
                final TocGenerator generator = new TocGenerator(wordprocessingMLPackage);
                generator.updateToc(false);
            }
        } catch (final TocException exception) {
            CustomDocxFormatter.LOG.error(
                "An error occurred during updating the Table Of Contents",
                exception
            );
        }
    }

    @Override
    public String formatValue(
        final Object value, final String name,
        final String fullname, final String function) {
        String result = super.formatValue(
            value, name, fullname, function
        );
        if (result != null && result.contains("\n")) {
            result = result.replace("\n", CustomDocxFormatter.NEWLINE_CHAR);
            this.newlines = true;
        }
        return result;
    }

    /**
     * Replaces newline characters with XML tags analogues.
     * @param part The part of XML document representation
     */
    private static void replaceNewLines(final MainDocumentPart part) {
        if (part != null) {
            String xml = XmlUtils.marshaltoString(part.getJaxbElement(), true, true);
            xml = xml.replace(CustomDocxFormatter.NEWLINE_CHAR, "</w:t><w:br/><w:t>");
            Object obj = null;
            try {
                obj = XmlUtils.unmarshalString(xml);
                part.setJaxbElement((Document) obj);
            } catch (final JAXBException exception) {
                CustomDocxFormatter.LOG.error(
                    "An error occurred during replacement of the newline character"
                );
            }
        }
    }
}
