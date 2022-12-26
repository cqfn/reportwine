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

package org.cqfn.reportwine.model;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.cqfn.reportwine.converters.YamlToIrConverter;
import org.cqfn.reportwine.exceptions.BaseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for IR model serialization to JSON.
 *
 * @since 0.1
 */
class JsonSerializerTest {
    /**
     * The folder with test resources.
     */
    private static final String TESTS_PATH = "src/test/sample/";

    /**
     * Test serialization of a text to JSON.
     */
    @Test
    void testTextSerialization() {
        final Text text = new Text("some text");
        Assertions.assertEquals("\"some text\"", text.toJson().toString());
    }

    /**
     * Test serialization of a text with number to JSON.
     */
    @Test
    void testNumericTextSerialization() {
        final Text text = new Text("3.14");
        Assertions.assertEquals(3.14, text.toJson().getAsDouble());
    }

    /**
     * Test serialization of a code to JSON.
     */
    @Test
    void testCodeSerialization() {
        final Code code = new Code("$ return project[current_phase]");
        Assertions.assertEquals("null", code.toJson().toString());
        Assertions.assertEquals("null", code.toJsonString());
    }

    /**
     * Test serialization of a simple pair to JSON.
     */
    @Test
    void testPairSerialization() {
        final Pair pair = new Pair("name", new Text("MyProject"));
        Assertions.assertEquals("{\"name\":\"MyProject\"}", pair.toJson().toString());
    }

    /**
     * Test serialization of pair array to JSON.
     */
    @Test
    void testNestedPairsSerialization() {
        final Pair pair = new Pair(
            "timeline", new Array(
                Arrays.asList(
                    new Pair("start_day", new Text("01.01.22")),
                    new Pair("end_day", new Text("31.08.22"))
                )
            )
        );
        Assertions.assertEquals(
            "{\"timeline\":{\"start_day\":\"01.01.22\",\"end_day\":\"31.08.22\"}}",
            pair.toJson().toString()
        );
    }

    /**
     * Test serialization of text array to JSON.
     */
    @Test
    void testTextArraySerialization() {
        final Array array = new Array(
            Arrays.asList(
                new Text("one"),
                new Text("two")
            )
        );
        Assertions.assertEquals(
            "[\"one\",\"two\"]",
            array.toJson().toString()
        );
    }

    /**
     * Test serialization of list to JSON.
     */
    @Test
    void testArrayListSerialization() {
        final List<Value> first = new LinkedList<>();
        first.add(new Pair("start", new Text("1")));
        first.add(new Pair("end", new Text("28")));
        final List<Value> second = new LinkedList<>();
        second.add(new Pair("start", new Text("2")));
        second.add(new Pair("end", new Text("31")));
        final Array array = new Array(
            Arrays.asList(
                new Array(first),
                new Array(second)
            )
        );
        Assertions.assertEquals(
            "[{\"start\":1,\"end\":28},{\"start\":2,\"end\":31}]",
            array.toJson().toString()
        );
    }

    /**
     * Test serialization of a complex IR structure to JSON with
     * pretty printing.
     */
    @Test
    void testComplexSerialization() {
        Pair pair = null;
        boolean oops = false;
        try {
            final YamlMapping yaml = Yaml.createYamlInput(
                new File(
                    JsonSerializerTest.TESTS_PATH.concat("complex_example.yml")
                )
            ).readYamlMapping();
            final YamlToIrConverter conv = new YamlToIrConverter(yaml);
            pair = conv.convert();
        } catch (final IOException | BaseException ignored) {
            oops = true;
        }
        Assertions.assertFalse(oops);
        Assertions.assertNotNull(pair);
        final String result = pair.toJsonString();
        oops = false;
        String expected = "";
        try {
            expected = JsonSerializerTest.readAsString(
                Paths.get(JsonSerializerTest.TESTS_PATH.concat("serialization_to_string.txt"))
            );
        } catch (final IOException exception) {
            oops = true;
        }
        Assertions.assertFalse(oops);
        Assertions.assertEquals(expected, result);
    }

    /**
     * Reads a file content as a string.
     * @param path The path to the file
     * @return The file content
     * @throws IOException If the file can't be read
     */
    private static String readAsString(final Path path) throws IOException {
        final InputStream stream = Files.newInputStream(path);
        final StringBuilder builder = new StringBuilder();
        for (int chr = stream.read(); chr != -1; chr = stream.read()) {
            if (chr != '\r') {
                builder.append((char) chr);
            }
        }
        stream.close();
        return builder.toString();
    }
}
