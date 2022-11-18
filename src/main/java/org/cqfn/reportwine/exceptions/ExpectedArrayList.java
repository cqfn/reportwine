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

package org.cqfn.reportwine.exceptions;

/**
 * Exception thrown while parsing internal representation of document bindings
 * when an array of only array values is expected, but another object is found in the array.
 *
 * @since 0.1
 */
public final class ExpectedArrayList extends BaseException {
    private static final long serialVersionUID = 6740286561286974225L;

    /**
     * The array.
     */
    private final String array;

    /**
     * Constructor.
     * @param array The array
     */
    public ExpectedArrayList(final String array) {
        super();
        this.array = array;
    }

    @Override
    public String getInitiator() {
        return "IrToYargConverter";
    }

    @Override
    public String getErrorMessage() {
        return new StringBuilder()
            .append("Expected array values in the list: '")
            .append(this.array)
            .append('\'')
            .toString();
    }
}
