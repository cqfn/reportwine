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

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import org.cqfn.reportwine.exceptions.BaseException;
import org.cqfn.reportwine.exceptions.UnsupportedYamlFormat;
import org.cqfn.reportwine.model.Array;
import org.cqfn.reportwine.model.Code;
import org.cqfn.reportwine.model.Pair;
import org.cqfn.reportwine.model.Text;
import org.cqfn.reportwine.model.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test for {@link YamlToIrConverterTest} class.
 *
 * @since 0.1
 */
class YamlToIrConverterTest {
    /**
     * Convert a YAML file without a root tag to IR.
     * @param dir The temporary directory
     */
    @Test
    void testDefaultIrRootName(@TempDir final Path dir) {
        final List<String> content = new LinkedList<>();
        content.add("project_name: MyProject");
        content.add("current_stage: 2");
        final Pair root = this.convertYamlToPair(dir, content);
        Assertions.assertEquals("Doc", root.getKey());
    }

    /**
     * Exception when a YAML file has unsupported format.
     * @param dir The temporary directory
     */
    @Test
    void testUnsupportedYamlFormatException(@TempDir final Path dir) throws BaseException {
        final List<String> content = new LinkedList<>();
        content.add("2");
        boolean oops = false;
        YamlMapping yaml = null;
        try {
            final Path path = this.createTempFile(dir, "yml", content);
            yaml = Yaml.createYamlInput(path.toFile()).readYamlMapping();
        } catch (final IOException ignored) {
            oops = true;
        }
        Assertions.assertFalse(oops);
        final YamlToIrConverter conv = new YamlToIrConverter(yaml);
        oops = false;
        try {
            conv.convert();
        } catch (final UnsupportedYamlFormat exception) {
            oops = true;
        }
        Assertions.assertTrue(oops);
    }

    /**
     * Test case: a YAML structure should be converted to the IR as a Pair.
     * @param dir The temporary directory
     */
    @Test
    void testConversionToPair(@TempDir final Path dir) {
        final List<String> content = new LinkedList<>();
        content.add("report:");
        content.add("  current_stage: 1");
        final Pair root = this.convertYamlToPair(dir, content);
        final Value value = root.getValue();
        Assertions.assertTrue(value instanceof Pair);
        final Pair pair = (Pair) value;
        Assertions.assertEquals("current_stage", pair.getKey());
        final Value data = pair.getValue();
        Assertions.assertTrue(data instanceof Text);
        Assertions.assertEquals("1", ((Text) data).getValue());
    }

    /**
     * Test case: a YAML structure should be converted to the IR as an Array.
     * @param dir The temporary directory
     */
    @Test
    void testConversionToArray(@TempDir final Path dir) {
        final List<String> content = new LinkedList<>();
        content.add("table:");
        content.add("  milestones:");
        content.add("    - description: First stage");
        content.add("      duration: 2");
        content.add("    - description: Second stage");
        content.add("      duration: 3");
        final Pair root = this.convertYamlToPair(dir, content);
        final Value value = root.getValue();
        Assertions.assertTrue(value instanceof Pair);
        final Value array = ((Pair) value).getValue();
        Assertions.assertTrue(array instanceof Array);
    }

    /**
     * Test case: a YAML structure should be converted to the IR as nested Pair objects.
     * @param dir The temporary directory
     */
    @Test
    void testConversionToPairWithPairValue(@TempDir final Path dir) {
        final List<String> content = new LinkedList<>();
        content.add("project:");
        content.add("  subproject:");
        content.add("    name: Part");
        final Pair root = this.convertYamlToPair(dir, content);
        final Value value = root.getValue();
        Assertions.assertTrue(value instanceof Pair);
        final Value pair = ((Pair) value).getValue();
        Assertions.assertTrue(pair instanceof Pair);
    }

    /**
     * Test case: a YAML structure should be converted to the IR as a Code.
     * @param dir The temporary directory
     */
    @Test
    void testConversionToCode(@TempDir final Path dir) {
        final List<String> content = new LinkedList<>();
        content.add("project:");
        content.add("  current_stage: 3");
        content.add("  previous_stage: \"$ return current_stage - 1;\"");
        final Pair pair = this.convertYamlToPair(dir, content);
        final Value value = pair.getValue();
        Assertions.assertTrue(value instanceof Array);
        final Array array = (Array) value;
        Assertions.assertEquals(2, array.size());
        final Value codepair = array.getValue(1);
        Assertions.assertTrue(codepair instanceof Pair);
        final Value code = ((Pair) codepair).getValue();
        Assertions.assertTrue(code instanceof Code);
        Assertions.assertEquals(" return current_stage - 1;", ((Code) code).getValue());
    }

    /**
     * Test case: a YAML structure with a sequence of literal block scalars
     * should be converted to the IR as a text array.
     * @param dir The temporary directory
     */
    @Test
    void testConversionFromSequenceOfLiterals(@TempDir final Path dir) {
        final List<String> content = new LinkedList<>();
        content.add("list:");
        content.add("  values:");
        content.add("    - |");
        content.add("      one");
        content.add("      two");
        content.add("    - |");
        content.add("      three");
        content.add("      four");
        content.add("      five");
        final Pair list = this.convertYamlToPair(dir, content);
        final Value value = list.getValue();
        Assertions.assertTrue(value instanceof Pair);
        final Pair pair = (Pair) value;
        final Value values = pair.getValue();
        Assertions.assertTrue(values instanceof Array);
        final Array array = (Array) values;
        Assertions.assertEquals(2, array.size());
        final Value first = array.getValue(0);
        Assertions.assertTrue(first instanceof Text);
        Assertions.assertEquals("one two", ((Text) first).getValue());
        final Value second = array.getValue(1);
        Assertions.assertTrue(second instanceof Text);
        Assertions.assertEquals("three four five", ((Text) second).getValue());
    }

    /**
     * Converts a YAML file to the IR.
     * @param dir The temporary directory
     * @param content The content of the YAML file
     */
    private Pair convertYamlToPair(final Path dir, final List<String> content) {
        boolean oops = false;
        YamlMapping yaml = null;
        try {
            final Path path = this.createTempFile(dir, "yml", content);
            yaml = Yaml.createYamlInput(path.toFile()).readYamlMapping();
        } catch (final IOException ignored) {
            oops = true;
        }
        Assertions.assertFalse(oops);
        final YamlToIrConverter conv = new YamlToIrConverter(yaml);
        oops = false;
        Pair pair = null;
        try {
            pair = conv.convert();
        } catch (final BaseException exception) {
            oops = true;
        }
        Assertions.assertFalse(oops);
        return pair;
    }

    /**
     * Creates a file in the temporary directory.
     * @param dir The temporary directory
     * @param extension The file extension
     * @param lines The file content
     * @return The path to a temporary file
     * @throws IOException If fails to create a temporary file
     */
    private Path createTempFile(
        final Path dir, final String extension, final List<String> lines)
        throws IOException {
        final Path file = dir.resolve("example.".concat(extension));
        Files.write(file, lines);
        return file;
    }
}
