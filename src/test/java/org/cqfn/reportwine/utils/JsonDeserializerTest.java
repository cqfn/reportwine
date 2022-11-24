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

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.cqfn.reportwine.converters.YamlToIrConverter;
import org.cqfn.reportwine.exceptions.BaseException;
import org.cqfn.reportwine.exceptions.UnsupportedJsonFormat;
import org.cqfn.reportwine.model.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link JsonDeserializer} class.
 *
 * @since 0.1
 */
class JsonDeserializerTest {
    /**
     * The folder with test resources.
     */
    private static final String TESTS_PATH = "src/test/sample/";

    /**
     * Test deserialization of a simple JSON object to IR pair.
     */
    @Test
    void loadIntoDraftNode() throws UnsupportedJsonFormat {
        final String source = "{ \"report\": { \"name\": \"MyProject\" } }";
        final JsonDeserializer deserializer = new JsonDeserializer(source);
        final Pair result = deserializer.convert();
        Assertions.assertNotNull(result);
        Assertions.assertEquals("report", result.getKey());
    }

    /**
     * Test deserialization of a complex JSON object to IR structure.
     */
    @Test
    void testComplexExample() throws UnsupportedJsonFormat {
        boolean oops = false;
        String source = "";
        try {
            source = JsonDeserializerTest.readAsString(
                Paths.get(JsonDeserializerTest.TESTS_PATH.concat("serialization_to_string.txt"))
            );
        } catch (final IOException ignored) {
            oops = true;
        }
        Assertions.assertFalse(oops);
        Pair expected = null;
        try {
            final YamlMapping yaml = Yaml.createYamlInput(
                new File(
                    JsonDeserializerTest.TESTS_PATH.concat("complex_example_no_code.yml")
                )
            ).readYamlMapping();
            final YamlToIrConverter conv = new YamlToIrConverter(yaml);
            expected = conv.convert();
        } catch (final IOException | BaseException ignored) {
            oops = true;
        }
        Assertions.assertFalse(oops);
        Assertions.assertNotNull(expected);
        final JsonDeserializer deserializer = new JsonDeserializer(source);
        final Pair result = deserializer.convert();
        Assertions.assertNotNull(result);
        Assertions.assertEquals(expected, result);
        Assertions.assertEquals(expected.toJsonString(), result.toJsonString());
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
