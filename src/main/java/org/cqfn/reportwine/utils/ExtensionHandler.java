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

import java.io.File;
import org.cqfn.reportwine.exceptions.ExpectedSimilarExtensions;

/**
 * Handler of extensions of files specified in CLI parameters.
 * Gets file extensions and checks if they are similar.
 *
 * @since 0.1
 */
public class ExtensionHandler {
    /**
     * The template file.
     */
    private final File template;

    /**
     * The result output file.
     */
    private final File output;

    /**
     * Constructor.
     * @param template The template file
     * @param output The result output file
     */
    public ExtensionHandler(final File template, final File output) {
        this.template = template;
        this.output = output;
    }

    /**
     * Returns an extension of files.
     * @return The extension
     * @throws ExpectedSimilarExtensions If files have different extensions
     */
    public String getExtension() throws ExpectedSimilarExtensions {
        final String inext = this.template.getName()
            .substring(this.template.getName().lastIndexOf('.') + 1);
        final String outext = this.output.getName()
            .substring(this.output.getName().lastIndexOf('.') + 1);
        if (inext.equals(outext)) {
            return inext;
        } else {
            throw ExpectedSimilarExtensions.INSTANCE;
        }
    }
}
