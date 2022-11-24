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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Objects;

/**
 * A key - value pair for data binding.
 *
 * @since 0.1
 */
public final class Pair implements Value {
    /**
     * The key name.
     */
    private final String key;

    /**
     * The value.
     */
    private Value value;

    /**
     * Constructor.
     * @param key The key name
     */
    public Pair(final String key) {
        this(key, null);
    }

    /**
     * Constructor.
     * @param key The key name
     * @param value The value
     */
    public Pair(final String key, final Value value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Binds a value to the key.
     * @param item The value
     */
    public void setValue(final Value item) {
        this.value = item;
    }

    /**
     * Returns the key.
     * @return The key
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Returns the value.
     * @return The value of the pair
     */
    public Value getValue() {
        return this.value;
    }

    @Override
    public String toJsonString() {
        final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
        return gson.toJson(this.toJson());
    }

    @Override
    public JsonElement toJson() {
        final JsonObject object = new JsonObject();
        object.add(this.key, this.value.toJson());
        return object;
    }

    @Override
    public boolean equals(final Object obj) {
        final Pair pair;
        boolean equal = false;
        if (obj instanceof Pair) {
            pair = (Pair) obj;
            if (this.key.equals(pair.getKey()) && this.value.equals(pair.getValue())) {
                equal = true;
            }
        }
        return equal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.key, this.value);
    }
}
