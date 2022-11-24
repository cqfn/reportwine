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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.cqfn.reportwine.exceptions.UnsupportedJsonFormat;
import org.cqfn.reportwine.model.Array;
import org.cqfn.reportwine.model.Pair;
import org.cqfn.reportwine.model.Text;
import org.cqfn.reportwine.model.Value;

/**
 * Converts a string that contains a JSON object to an intermediate representation (IR).
 *
 * @since 0.1
 */
public class JsonDeserializer {
    /**
     * The string that contains a JSON object.
     */
    private final String json;

    /**
     * Constructor.
     * @param json A string that contains a JSON object
     */
    public JsonDeserializer(final String json) {
        this.json = json;
    }

    /**
     * Converts the source string that contains a JSON object to an IR.
     * @return A root pair
     * @throws UnsupportedJsonFormat If an error occurs during JSON parsing
     */
    public Pair convert() throws UnsupportedJsonFormat {
        Pair result = null;
        final JsonElement element = new Gson().fromJson(this.json, JsonElement.class);
        if (element.isJsonObject()) {
            final JsonObject obj = element.getAsJsonObject();
            final Set<String> keys = obj.keySet();
            if (keys.size() > 1) {
                throw UnsupportedJsonFormat.INSTANCE;
            }
            final Optional<String> optional = keys.stream().findFirst();
            if (optional.isPresent()) {
                final String key = optional.get();
                result = new Pair(key, this.convertElement(obj.get(key)));
            }
        }
        return result;
    }

    /**
     * Converts the JSON element to an IR value.
     * @param element The JSON element
     * @return An IR value
     */
    private Value convertElement(final JsonElement element) {
        Value result = null;
        if (element.isJsonArray()) {
            final List<Value> list = new LinkedList<>();
            for (final JsonElement child : element.getAsJsonArray()) {
                list.add(this.convertElement(child));
            }
            result = new Array(list);
        } else if (element.isJsonPrimitive()) {
            result = new Text(element.getAsString());
        } else if (element.isJsonObject()) {
            result = this.convertObject(element.getAsJsonObject());
        }
        return result;
    }

    /**
     * Converts the JSON object to an IR value.
     * @param object The JSON object
     * @return An IR value
     */
    private Value convertObject(final JsonObject object) {
        final List<Value> list = new LinkedList<>();
        final Value result;
        if (object.keySet().size() == 1) {
            final String key = object.keySet().stream().findFirst().get();
            result = new Pair(key, this.convertElement(object.get(key)));
        } else {
            for (final String key : object.keySet()) {
                final Value value = this.convertElement(object.get(key));
                if (value != null) {
                    final Pair pair = new Pair(key, value);
                    list.add(pair);
                }
            }
            result = new Array(list);
        }
        return result;
    }
}
