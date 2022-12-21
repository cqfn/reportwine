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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import org.docx4j.jaxb.XPathBinderAssociationIsPartialException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Text;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test for {@link DocxGenerator} class.
 *
 * @since 0.1
 */
class DocxGeneratorTest {
    /**
     * The folder with test resources.
     */
    private static final String TESTS_PATH = "src/test/sample/";

    /**
     * Examples of data bindings for testing.
     */
    private final TestBandDataCreator data = new TestBandDataCreator();

    /**
     * Test generation of docx report on simple text replacements.
     * @param source A temporary directory
     */
    @Test
    void testSimpleDocxReportGeneration(@TempDir final Path source) {
        final DocxGenerator generator = new DocxGenerator(this.data.simpleExample());
        boolean caught = false;
        try {
            generator.renderDocument(
                new File(DocxGeneratorTest.TESTS_PATH.concat("template.docx")),
                source.resolve("report.docx").toFile()
            );
        } catch (final IOException exception) {
            caught = true;
        }
        Assertions.assertFalse(caught);
        WordprocessingMLPackage expected = null;
        WordprocessingMLPackage actual = null;
        try {
            expected = WordprocessingMLPackage.load(
                new File(DocxGeneratorTest.TESTS_PATH.concat("result_expected.docx"))
            );
            actual = WordprocessingMLPackage.load(
                source.resolve("report.docx").toFile()
            );
        } catch (final Docx4JException exception) {
            caught = true;
        }
        Assertions.assertFalse(caught);
        Assertions.assertNotNull(expected);
        Assertions.assertNotNull(actual);
        final String exptext = this.collectAllText(expected.getMainDocumentPart());
        final String acttext = this.collectAllText(actual.getMainDocumentPart());
        Assertions.assertEquals(exptext, acttext);
    }

    /**
     * Test generation of docx report on complex replacements.
     * @param source A temporary directory
     */
    @Test
    void testComplexDocxReportGeneration(@TempDir final Path source) {
        final DocxGenerator generator = new DocxGenerator(this.data.complexExample());
        boolean caught = false;
        try {
            generator.renderDocument(
                new File(DocxGeneratorTest.TESTS_PATH.concat("complex_template.docx")),
                source.resolve("report.docx").toFile()
            );
        } catch (final IOException exception) {
            caught = true;
        }
        Assertions.assertFalse(caught);
        WordprocessingMLPackage expected = null;
        WordprocessingMLPackage actual = null;
        try {
            expected = WordprocessingMLPackage.load(
                new File(DocxGeneratorTest.TESTS_PATH.concat("complex_result_expected.docx"))
            );
            actual = WordprocessingMLPackage.load(
                source.resolve("report.docx").toFile()
            );
        } catch (final Docx4JException exception) {
            caught = true;
        }
        Assertions.assertFalse(caught);
        Assertions.assertNotNull(expected);
        Assertions.assertNotNull(actual);
        final String exptext = this.collectAllText(expected.getMainDocumentPart());
        final String acttext = this.collectAllText(actual.getMainDocumentPart());
        Assertions.assertEquals(exptext, acttext);
    }

    /**
     * Sequentially collects all values from text tags in the document, deletes all spaces and
     * concatenates them into a single string.
     * @param part The main document part
     * @return The result string
     */
    private String collectAllText(final MainDocumentPart part) {
        final StringBuilder builder = new StringBuilder();
        final String xpath = "//w:t";
        boolean caught = false;
        try {
            final List<Object> list = part.getJAXBNodesViaXPath(xpath, false);
            for (final Object node : list) {
                if (node instanceof JAXBElement) {
                    final Object value = ((JAXBElement) node).getValue();
                    if (value instanceof Text) {
                        final String text = ((Text) value).getValue();
                        builder.append(text.replaceAll("\\s", ""));
                    }
                }
            }
        } catch (final JAXBException | XPathBinderAssociationIsPartialException exception) {
            caught = true;
        }
        Assertions.assertFalse(caught);
        return builder.toString();
    }
}
