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

package org.cqfn.reportwine.generators;

import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.impl.ReportTemplateImpl;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.io.IOUtils;
import org.cqfn.reportwine.formatters.CustomDocxFormatter;

/**
 * Generator of docx files.
 *
 * @since 0.1
 */
public class DocxGenerator {
    /**
     * The data to be inserted into the placeholders in the document template.
     */
    private final BandData mappings;

    /**
     * Constructor.
     * @param mappings The {@link BandData} object with YARG mappings
     */
    public DocxGenerator(final BandData mappings) {
        this.mappings = mappings;
    }

    /**
     * Renders a document with data collected from the YAML file.
     * @param template The template file
     * @param output The file to store the generated document
     * @throws IOException If an error during input or output actions occurs
     */
    public void renderDocument(final File template, final File output) throws IOException {
        final OutputStream stream = Files.newOutputStream(Paths.get(output.getPath()));
        final ReportTemplateImpl docxreport = new ReportTemplateImpl(
            "",
            template.getName(),
            template.getPath(),
            ReportOutputType.docx
        );
        final CustomDocxFormatter docformatter = new CustomDocxFormatter(
            new FormatterFactoryInput(
                "docx",
                this.mappings,
                docxreport,
                stream
            )
        );
        docformatter.renderDocument();
        IOUtils.closeQuietly(stream);
    }
}
