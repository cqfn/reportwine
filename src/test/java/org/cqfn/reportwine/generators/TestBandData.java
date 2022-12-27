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

import com.haulmont.yarg.structure.BandData;
import java.util.Arrays;

/**
 * Data bindings of {@link BandData} for testing.
 *
 * @since 0.1
 */
public class TestBandData {
    /**
     * The structure for simple example.
     */
    private final BandData simple;

    /**
     * The structure for complex example.
     */
    private final BandData complex;

    /**
     * Constructor.
     * Creates a simple and a complex examples.
     */
    public TestBandData() {
        this.simple = TestBandData.createSimpleExample();
        this.complex = TestBandData.createComplexExample();
    }

    /**
     * Returns a simple example.
     * @return The simple example of data bindings
     */
    public BandData simpleExample() {
        return this.simple;
    }

    /**
     * Returns a complex example.
     * @return The complex example of data bindings
     */
    public BandData complexExample() {
        return this.complex;
    }

    /**
     * Creates a simple example.
     * @return The simple example of data bindings
     */
    private static BandData createSimpleExample() {
        final BandData project = new BandData("project");
        project.addData("name", "Reportwine");
        project.addData(
            "about", "The goal of the work is to reduce time spent on a report creation."
        );
        project.addData(
            "main_steps",
            String.format(
                "%s\n%s\n%s", "implement idea", "release the project", "evaluate the results"
            )
        );
        return project;
    }

    /**
     * Creates a complex example.
     * @return The complex example of data bindings
     */
    private static BandData createComplexExample() {
        final BandData project = new BandData("project");
        project.addData("name", "MyResearch");
        project.addData(
            "about",
            String.format(
                "%s%s",
                "The goal of work is to explore, collect data, ",
                "process it, analyze, implement approach and write a paper."
            )
        );
        project.addData("current_phase", "2");
        project.addData("goals", "one\ntwo\nthree\n");
        final BandData first = new BandData("milestones", project);
        first.addData("description", "First stage");
        first.addData("duration", "3");
        first.addData("objectives", "Explore and collect");
        final BandData second = new BandData("milestones", project);
        second.addData("description", "Second stage");
        second.addData("duration", "4");
        second.addData("objectives", "Process and analyze");
        final BandData third = new BandData("milestones", project);
        third.addData("description", "Last stage");
        third.addData("duration", "4");
        third.addData("objectives", "Implement\nWrite paper\n");
        final BandData timeline = new BandData("timeline", project);
        timeline.addData("start_date", "2020");
        final BandData end = new BandData("end_date", timeline);
        end.addData("planned", "2022");
        end.addData("actual", "2023");
        timeline.addChild(end);
        project.addChildren(Arrays.asList(first, second, third, timeline));
        return project;
    }
}
