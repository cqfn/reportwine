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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.cqfn.reportwine.exceptions.UnsupportedJsonFormat;
import org.cqfn.reportwine.model.IrMerger;
import org.cqfn.reportwine.model.Pair;

/**
 * Executor of JavaScript code that changes model.
 *
 * @since 0.1
 */
public class JsExecutor {
    /**
     * The root node of the model.
     */
    private final Pair root;

    /**
     * Constructor.
     * @param root The root node of the model
     */
    public JsExecutor(final Pair root) {
        this.root = root;
    }

    /**
     * Executes JS code and returns model with changes.
     * @param variable The name of variable which value will be calculated
     * @param code JS code
     * @return Model with changes
     */
    public Pair exec(final String variable, final String code) {
        final String json = this.root.toJsonString();
        final List<String> list = Arrays.asList(
            String.format("var model = %s;", json),
            "var method = function() {",
            code,
            "};",
            String.format(
                "model.%s.%s = method.apply(model.%s);",
                this.root.getKey(),
                variable,
                this.root.getKey()
            ),
            "JSON.stringify(model)"
        );
        final String source = String.join("\n", list);
        final JsInterpreter interpreter = new JsInterpreter(source);
        final String changed = interpreter.runScript(Collections.emptyMap());
        final JsonDeserializer deserializer = new JsonDeserializer(changed);
        Pair result;
        try {
            final Pair deserialized =  deserializer.convert();
            final IrMerger merger = new IrMerger();
            result = merger.merge(this.root, deserialized);
        } catch (final UnsupportedJsonFormat ignored) {
            result = this.root;
        }
        return result;
    }
}
