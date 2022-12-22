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

import com.haulmont.yarg.structure.BandData;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.cqfn.reportwine.converters.YargToDocx4jConverter;
import org.docx4j.dml.CTRegularTextRun;
import org.docx4j.jaxb.XPathBinderAssociationIsPartialException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pptx4j.Pptx4jException;

/**
 * Test for {@link PptxGenerator} class.
 *
 * @since 0.1
 */
class PptxGeneratorTest {
    /**
     * The folder with test resources.
     */
    private static final String TESTS_PATH = "src/test/sample/";

    /**
     * Examples of data bindings for testing.
     */
    private final TestBandData data = new TestBandData();

    /**
     * Test generation of pptx report on simple text replacements.
     * @param source A temporary directory
     */
    @Test
    void testSimplePptxReportGeneration(@TempDir final Path source) {
        this.checkContentEquality(
            this.data.simpleExample(),
            "simple",
            source
        );
    }

    /**
     * Test generation of pptx report on complex replacements.
     * @param source A temporary directory
     */
    @Test
    void testComplexPptxReportGeneration(@TempDir final Path source) {
        this.checkContentEquality(
            this.data.complexExample(),
            "complex",
            source
        );
    }

    /**
     * Collects relevant data from expected and actual files and checks if it is equal.
     * @param band Data bindings to be inserted into the template
     * @param prefix The prefix of the file name
     * @param source A temporary directory
     */
    private void checkContentEquality(
        final BandData band, final String prefix, @TempDir final Path source) {
        final YargToDocx4jConverter converter = new YargToDocx4jConverter(band);
        final PptxGenerator generator = new PptxGenerator(
            converter.convert(), converter.getTables()
        );
        boolean caught = false;
        try {
            generator.renderDocument(
                new File(
                    String.format("%s%s_template.pptx", PptxGeneratorTest.TESTS_PATH, prefix)
                ),
                source.resolve("report.pptx").toFile()
            );
        } catch (final Docx4JException | Pptx4jException exception) {
            caught = true;
        }
        Assertions.assertFalse(caught);
        PresentationMLPackage expected = null;
        PresentationMLPackage actual = null;
        try {
            expected = PresentationMLPackage.load(
                new File(
                    String.format(
                        "%s%s_result_expected.pptx",
                        PptxGeneratorTest.TESTS_PATH, prefix
                    )
                )
            );
            actual = PresentationMLPackage.load(
                source.resolve("report.pptx").toFile()
            );
        } catch (final Docx4JException exception) {
            caught = true;
        }
        Assertions.assertFalse(caught);
        Assertions.assertNotNull(expected);
        Assertions.assertNotNull(actual);
        final StringBuilder exptext = new StringBuilder();
        final StringBuilder acttext = new StringBuilder();
        try {
            final List<SlidePart> expslides = expected.getMainPresentationPart().getSlideParts();
            for (final SlidePart slide : expslides) {
                exptext.append(this.collectAllText(slide));
            }
            final List<SlidePart> actlides = actual.getMainPresentationPart().getSlideParts();
            for (final SlidePart slide : actlides) {
                acttext.append(this.collectAllText(slide));
            }
        } catch (final Pptx4jException exception) {
            caught = true;
        }
        Assertions.assertFalse(caught);
        Assertions.assertEquals(exptext.toString(), acttext.toString());
    }

    /**
     * Sequentially collects all values from text tags in the slide, deletes all spaces and
     * concatenates them into a single string.
     * @param part The slide part
     * @return The result string
     */
    private String collectAllText(final SlidePart part) {
        final StringBuilder builder = new StringBuilder();
        final String xpath = "//a:r";
        boolean caught = false;
        try {
            final List<Object> list = part.getJAXBNodesViaXPath(xpath, false);
            for (final Object node : list) {
                if (node instanceof CTRegularTextRun) {
                    final String value = ((CTRegularTextRun) node).getT();
                    if (value != null) {
                        builder.append(value.replaceAll("\\s", ""));
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
