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
import java.io.IOException;
import java.nio.file.Path;
import org.cqfn.reportwine.exceptions.BaseException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pptx4j.Pptx4jException;

/**
 * Test for {@link Main} class.
 *
 * @since 0.1
 */
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
     * Test passing required options to main().
     * @param source A temporary directory
     */
    @Test
    void testNoException(@TempDir final Path source) {
        final String[] example = {
            MainTest.TEMPLATE,
            MainTest.TESTS_PATH.concat("template.docx"),
            MainTest.OUTPUT,
            source.resolve("report.docx").toString(),
            MainTest.PROJECT,
            MainTest.TESTS_PATH.concat("project_valid.yml"),
        };
        boolean caught = false;
        try {
            Main.main(example);
        } catch (final BaseException | IOException | ParameterException
            | Pptx4jException | Docx4JException exc) {
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
            MainTest.TESTS_PATH.concat("template.docx"),
            MainTest.OUTPUT,
            source.resolve("report.docx").toString(),
            MainTest.PROJECT,
            MainTest.TESTS_PATH.concat("project_valid.yml"),
            "--config",
            MainTest.TESTS_PATH.concat("config.yml"),
        };
        boolean caught = false;
        try {
            Main.main(example);
        } catch (final BaseException | IOException | ParameterException
            | Pptx4jException | Docx4JException exc) {
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
        } catch (final BaseException | IOException | ParameterException
            | Pptx4jException | Docx4JException exc) {
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
        throws IOException, Pptx4jException, Docx4JException {
        final String[] example = {
            MainTest.TEMPLATE,
            MainTest.TESTS_PATH.concat("template.docx"),
            MainTest.OUTPUT,
            source.resolve("report.docx").toString(),
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
        final String expectedmsg = "Expected scalar value in the array: '- implement idea";
        Assertions.assertTrue(message.startsWith(expectedmsg));
    }
}
