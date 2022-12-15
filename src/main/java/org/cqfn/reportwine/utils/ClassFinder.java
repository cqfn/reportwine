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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.docx4j.TraversalUtil;

/**
 * Finder of Docx4j objects.
 *
 * @since 0.1
 */
public final class ClassFinder extends TraversalUtil.CallbackImpl {
    /**
     * The list of found objects.
     */
    private final List<Object> objects;

    /**
     * The searchable type of objects.
     */
    private final Class<?> type;

    /**
     * Constructor.
     * @param type The type of objects to be found
     */
    public ClassFinder(final Class<?> type) {
        this.type = type;
        this.objects = new LinkedList<>();
    }

    @Override
    public List<Object> apply(final Object obj) {
        if (obj.getClass().equals(this.type)) {
            this.objects.add(obj);
        }
        return Collections.emptyList();
    }

    @Override
    public List<Object> getChildren(final Object obj) {
        final List<Object> result;
        if (obj instanceof org.pptx4j.pml.CTGraphicalObjectFrame) {
            final org.docx4j.dml.Graphic graphic =
                ((org.pptx4j.pml.CTGraphicalObjectFrame) obj).getGraphic();
            if (graphic == null || graphic.getGraphicData() == null) {
                result = null;
            } else {
                result = graphic.getGraphicData().getAny();
            }
        } else {
            result = TraversalUtil.getChildrenImpl(obj);
        }
        return result;
    }

    /**
     * Returns found objects of the specified class type.
     * @return The list of collected objects
     */
    public List<Object> getObjects() {
        return this.objects;
    }
}
