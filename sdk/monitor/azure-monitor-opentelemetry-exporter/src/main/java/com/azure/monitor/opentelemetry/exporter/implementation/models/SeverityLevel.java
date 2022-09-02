/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.azure.monitor.opentelemetry.exporter.implementation.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/**
 * Defines values for SeverityLevel.
 */
public final class SeverityLevel extends ExpandableStringEnum<SeverityLevel> {
    /**
     * Static value Verbose for SeverityLevel.
     */
    public static final SeverityLevel VERBOSE = fromString("Verbose");

    /**
     * Static value Information for SeverityLevel.
     */
    public static final SeverityLevel INFORMATION = fromString("Information");

    /**
     * Static value Warning for SeverityLevel.
     */
    public static final SeverityLevel WARNING = fromString("Warning");

    /**
     * Static value Error for SeverityLevel.
     */
    public static final SeverityLevel ERROR = fromString("Error");

    /**
     * Static value Critical for SeverityLevel.
     */
    public static final SeverityLevel CRITICAL = fromString("Critical");

    /**
     * Creates or finds a SeverityLevel from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding SeverityLevel.
     */
    @JsonCreator
    public static SeverityLevel fromString(String name) {
        return fromString(name, SeverityLevel.class);
    }

    /**
     * @return known SeverityLevel values.
     */
    public static Collection<SeverityLevel> values() {
        return values(SeverityLevel.class);
    }
}
