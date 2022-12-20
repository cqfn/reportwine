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

package org.cqfn.reportwine;

import com.beust.jcommander.ParameterException;
import io.github.netmikey.logunit.api.LogCapturer;
import java.io.IOException;
import java.nio.file.Path;
import org.cqfn.reportwine.exceptions.BaseException;
import org.cqfn.reportwine.exceptions.UnsupportedYamlFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test for {@link Main} class.
 *
 * @since 0.1
 */
@SuppressWarnings("PMD.TooManyMethods")
class MainTest {
    /**
     * The "--template" option.
     */
    private static final String TEMPLATE = "--template";

    /**
     * The "--output" option.
     */
    private static final String OUTPUT = "--output";

    /**
     * The "--project" option.
     */
    private static final String PROJECT = "--project";

    /**
     * The folder with test resources.
     */
    private static final String TESTS_PATH = "src/test/sample/";

    /**
     * The "template.docx" file.
     */
    private static final String TEMPLATE_DOCX = "template.docx";

    /**
     * The "report.docx" file.
     */
    private static final String REPORT_DOCX = "report.docx";

    /**
     * The "project_valid" file.
     */
    private static final String PROJECT_VALID = "project_valid.yml";

    /**
     * The capturer for Logger.
     */
    @RegisterExtension
    private final LogCapturer logs = LogCapturer.create().captureForType(Main.class);

    /**
     * Test generation of docx report passing required options to main().
     * @param source A temporary directory
     */
    @Test
    void testDocxNoException(@TempDir final Path source) {
        final String[] example = {
            MainTest.TEMPLATE,
            MainTest.TESTS_PATH.concat(MainTest.TEMPLATE_DOCX),
            MainTest.OUTPUT,
            source.resolve(MainTest.REPORT_DOCX).toString(),
            MainTest.PROJECT,
            MainTest.TESTS_PATH.concat(MainTest.PROJECT_VALID),
        };
        boolean caught = false;
        try {
            Main.main(example);
        } catch (final BaseException | IOException | ParameterException exc) {
            caught = true;
        }
        Assertions.assertFalse(caught);
    }

    /**
     * Test passing required and additional options to main().
     * @param source A temporary directory
     */
    @Test
    void testNoExceptionWithConfig(@TempDir final Path source) {
        final String[] example = {
            MainTest.TEMPLATE,
            MainTest.TESTS_PATH.concat(MainTest.TEMPLATE_DOCX),
            MainTest.OUTPUT,
            source.resolve(MainTest.REPORT_DOCX).toString(),
            MainTest.PROJECT,
            MainTest.TESTS_PATH.concat(MainTest.PROJECT_VALID),
            "--config",
            MainTest.TESTS_PATH.concat("config.yml"),
        };
        boolean caught = false;
        try {
            Main.main(example);
        } catch (final BaseException | IOException | ParameterException exc) {
            caught = true;
        }
        Assertions.assertFalse(caught);
    }

    /**
     * Test passing no option to main().
     */
    @Test
    void testWithException() {
        final String[] example = {
        };
        boolean caught = false;
        try {
            Main.main(example);
        } catch (final BaseException | IOException | ParameterException exc) {
            caught = true;
        }
        Assertions.assertTrue(caught);
    }

    /**
     * Test passing YAML file with unsupported content.
     * @param source A temporary directory
     */
    @Test
    void testWithInvalidYaml(@TempDir final Path source)
        throws IOException {
        final String[] example = {
            MainTest.TEMPLATE,
            MainTest.TESTS_PATH.concat(MainTest.TEMPLATE_DOCX),
            MainTest.OUTPUT,
            source.resolve(MainTest.REPORT_DOCX).toString(),
            MainTest.PROJECT,
            MainTest.TESTS_PATH.concat("project_invalid.yml"),
        };
        boolean caught = false;
        String message = "";
        try {
            Main.main(example);
        } catch (final BaseException exception) {
            caught = true;
            message = exception.getErrorMessage();
        }
        Assertions.assertTrue(caught);
        final StringBuilder builder = new StringBuilder(64);
        builder
            .append("Expected scalar value in the array: ")
            .append(System.lineSeparator())
            .append("- implement idea");
        Assertions.assertTrue(message.startsWith(builder.toString()));
    }

    /**
     * Test passing not existing YAML file.
     * @param source A temporary directory
     */
    @Test
    void testPassingNotFoundYaml(@TempDir final Path source)
        throws BaseException {
        final String[] example = {
            MainTest.TEMPLATE,
            MainTest.TESTS_PATH.concat(MainTest.TEMPLATE_DOCX),
            MainTest.OUTPUT,
            source.resolve(MainTest.REPORT_DOCX).toString(),
            MainTest.PROJECT,
            MainTest.TESTS_PATH.concat("not_existing.yml"),
        };
        boolean caught = false;
        try {
            Main.main(example);
        } catch (final IOException exception) {
            caught = true;
        }
        Assertions.assertTrue(caught);
        this.logs.assertContains("Cannot read YAML file");
    }

    /**
     * Test passing not existing pptx file.
     * @param source A temporary directory
     */
    @Test
    void testPassingNotFoundPptx(@TempDir final Path source) {
        final String[] example = {
            MainTest.TEMPLATE,
            MainTest.TESTS_PATH.concat("pattern.pptx"),
            MainTest.OUTPUT,
            source.resolve("report.pptx").toString(),
            MainTest.PROJECT,
            MainTest.TESTS_PATH.concat(MainTest.PROJECT_VALID),
        };
        boolean caught = false;
        try {
            Main.main(example);
        } catch (final IOException | BaseException exception) {
            caught = true;
        }
        Assertions.assertFalse(caught);
        this.logs.assertContains("Cannot load pptx template");
    }

    /**
     * Test passing empty pptx file, without slides.
     * @param source A temporary directory
     */
    @Test
    void testPassingEmptyPptx(@TempDir final Path source) {
        final String[] example = {
            MainTest.TEMPLATE,
            MainTest.TESTS_PATH.concat("empty.pptx"),
            MainTest.OUTPUT,
            source.resolve("report.pptx").toString(),
            MainTest.PROJECT,
            MainTest.TESTS_PATH.concat(MainTest.PROJECT_VALID),
        };
        boolean caught = false;
        try {
            Main.main(example);
        } catch (final IOException | BaseException exception) {
            caught = true;
        }
        Assertions.assertFalse(caught);
        this.logs.assertContains("Cannot find pptx template slides");
    }

    /**
     * Test passing YAML file of unsupported format.
     * @param source A temporary directory
     */
    @Test
    void testUnsupportedYamlFormat(@TempDir final Path source)
        throws IOException, BaseException {
        final String[] example = {
            MainTest.TEMPLATE,
            MainTest.TESTS_PATH.concat(MainTest.TEMPLATE_DOCX),
            MainTest.OUTPUT,
            source.resolve(MainTest.REPORT_DOCX).toString(),
            MainTest.PROJECT,
            MainTest.TESTS_PATH.concat("unsupported.yml"),
        };
        boolean caught = false;
        try {
            Main.main(example);
        } catch (final UnsupportedYamlFormat exception) {
            caught = true;
        }
        Assertions.assertTrue(caught);
        this.logs.assertContains("Unsupported structure of input YAML");
    }

    /**
     * Test passing YAML file which data cannot be converted to YARG bindings.
     * @param source A temporary directory
     */
    @Test
    void testCannotCastToYargBindings(@TempDir final Path source)
        throws IOException {
        final String[] example = {
            MainTest.TEMPLATE,
            MainTest.TESTS_PATH.concat(MainTest.TEMPLATE_DOCX),
            MainTest.OUTPUT,
            source.resolve(MainTest.REPORT_DOCX).toString(),
            MainTest.PROJECT,
            MainTest.TESTS_PATH.concat("not_converted_to_yarg.yml"),
        };
        boolean caught = false;
        String message = "";
        try {
            Main.main(example);
        } catch (final BaseException exception) {
            caught = true;
            message = exception.getErrorMessage();
        }
        Assertions.assertTrue(caught);
        Assertions.assertTrue(message.startsWith("Expected array values in the list"));
        this.logs.assertContains("Cannot cast data to docx bindings");
    }

    /**
     * Test generation of docx report passing required options to main().
     * @param source A temporary directory
     */
    @Test
    void testPptxNoException(@TempDir final Path source) {
        final String[] example = {
            MainTest.TEMPLATE,
            MainTest.TESTS_PATH.concat("template.pptx"),
            MainTest.OUTPUT,
            source.resolve("report.pptx").toString(),
            MainTest.PROJECT,
            MainTest.TESTS_PATH.concat("complex_example.yml"),
        };
        boolean caught = false;
        try {
            Main.main(example);
        } catch (final BaseException | IOException | ParameterException exc) {
            caught = true;
        }
        Assertions.assertFalse(caught);
    }

    /**
     * Test specifying a template and output file with different extensions.
     * @param source A temporary directory
     */
    @Test
    void testWithDifferentFileExtensions(@TempDir final Path source)
        throws IOException {
        final String[] example = {
            MainTest.TEMPLATE,
            MainTest.TESTS_PATH.concat(MainTest.TEMPLATE_DOCX),
            MainTest.OUTPUT,
            source.resolve("report.pptx").toString(),
            MainTest.PROJECT,
            MainTest.TESTS_PATH.concat(MainTest.PROJECT_VALID),
        };
        boolean caught = false;
        try {
            Main.main(example);
        } catch (final BaseException exception) {
            caught = true;
        }
        Assertions.assertFalse(caught);
        this.logs.assertContains("Template and output files should have similar extensions");
    }
}
