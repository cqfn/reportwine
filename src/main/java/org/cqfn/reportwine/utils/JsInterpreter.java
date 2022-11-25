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

import java.util.Map;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

/**
 * Interpreter of JavaScript code.
 *
 * @since 0.1
 */
public class JsInterpreter {
    /**
     * The script in JavaScript.
     */
    private final String script;

    /**
     * Constructor.
     * @param script The script
     */
    public JsInterpreter(final String script) {
        this.script = script;
    }

    /**
     * Runs JavaScript code with specified parameters.
     * @param params The parameters passed into script
     * @return The result of the script invocation
     */
    public String runScript(final Map<String, Object> params) {
        final Context context = Context.create("js");
        final Value bindings = context.getBindings("js");
        for (final Map.Entry<String, Object> param : params.entrySet()) {
            bindings.putMember(param.getKey(), param.getValue());
        }
        final Value value = context.eval("js", this.script);
        return value.asString();
    }
}
