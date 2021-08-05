// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for SeverityLevel. */
public final class SeverityLevel extends ExpandableStringEnum<SeverityLevel> {
    /** Static value Verbose for SeverityLevel. */
    public static final SeverityLevel VERBOSE = fromString("Verbose");

    /** Static value Information for SeverityLevel. */
    public static final SeverityLevel INFORMATION = fromString("Information");

    /** Static value Warning for SeverityLevel. */
    public static final SeverityLevel WARNING = fromString("Warning");

    /** Static value Error for SeverityLevel. */
    public static final SeverityLevel ERROR = fromString("Error");

    /** Static value Critical for SeverityLevel. */
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

    /** @return known SeverityLevel values. */
    public static Collection<SeverityLevel> values() {
        return values(SeverityLevel.class);
    }
}
