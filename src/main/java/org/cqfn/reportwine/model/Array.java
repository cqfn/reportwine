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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * An array of values.
 *
 * @since 0.1
 */
public final class Array implements Value {
    /**
     * The linked list of values.
     */
    private final List<Value> values;

    /**
     * Constructor.
     * @param items The list of values
     */
    public Array(final List<Value> items) {
        this.values = new LinkedList<>(items);
    }

    /**
     * Checks if the array is a list of texts.
     * @return Checking result, {@code true} if the array is an array of texts
     *  or {@code false} otherwise
     */
    public boolean isTextArray() {
        boolean array = true;
        for (final Value value : this.values) {
            if (!(value instanceof Text)) {
                array = false;
                break;
            }
        }
        return array;
    }

    /**
     * Checks if the array is a list of arrays, i.e, a structure
     * that specifies a table.
     * @return Checking result, {@code true} if the array is of arrays
     *  or {@code false} otherwise
     */
    public boolean isArrayList() {
        boolean text = true;
        for (final Value value : this.values) {
            if (!(value instanceof Array)) {
                text = false;
                break;
            }
        }
        return text;
    }

    /**
     * Checks if the array is a list of pairs.
     * @return Checking result, {@code true} if the array is an array of pairs
     *  or {@code false} otherwise
     */
    public boolean isPairArray() {
        boolean pair = true;
        for (final Value value : this.values) {
            if (!(value instanceof Pair)) {
                pair = false;
                break;
            }
        }
        return pair;
    }

    /**
     * Count a size of the array.
     * @return The number of elements in the array
     */
    public int size() {
        return this.values.size();
    }

    /**
     * Returns values of the array.
     * @return The list of values
     */
    public List<Value> getValues() {
        return this.values;
    }

    /**
     * Returns a value by its index.
     * @param index The index
     * @return The value
     * @throws IndexOutOfBoundsException If the index is wrong
     */
    public Value getValue(final int index) throws IndexOutOfBoundsException {
        return this.values.get(index);
    }

    @Override
    public String toJsonString() {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this.toJson());
    }

    @Override
    public JsonElement toJson() {
        JsonElement element = JsonNull.INSTANCE;
        if (this.isPairArray()) {
            final JsonObject object = new JsonObject();
            for (final Value value : this.values) {
                final Pair pair = (Pair) value;
                object.add(pair.getKey(), pair.getValue().toJson());
            }
            element = object;
        }
        if (this.isTextArray()) {
            final JsonArray array = new JsonArray();
            for (final Value value : this.values) {
                final Text text = (Text) value;
                array.add(text.toJson());
            }
            element = array;
        }
        if (this.isArrayList()) {
            final JsonArray array = new JsonArray();
            for (final Value value : this.values) {
                final Array item = (Array) value;
                array.add(item.toJson());
            }
            element = array;
        }
        return element;
    }

    @Override
    public boolean equals(final Object obj) {
        final Array array;
        boolean equal = false;
        if (obj instanceof Array) {
            array = (Array) obj;
            equal = Arrays.deepEquals(this.values.toArray(), array.getValues().toArray());
        }
        return equal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.values);
    }
}
