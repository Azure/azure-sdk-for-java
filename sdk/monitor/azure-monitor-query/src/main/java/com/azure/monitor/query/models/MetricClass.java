// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for MetricClass. */
public final class MetricClass extends ExpandableStringEnum<MetricClass> {
    /** Static value Availability for MetricClass. */
    public static final MetricClass AVAILABILITY = fromString("Availability");

    /** Static value Transactions for MetricClass. */
    public static final MetricClass TRANSACTIONS = fromString("Transactions");

    /** Static value Errors for MetricClass. */
    public static final MetricClass ERRORS = fromString("Errors");

    /** Static value Latency for MetricClass. */
    public static final MetricClass LATENCY = fromString("Latency");

    /** Static value Saturation for MetricClass. */
    public static final MetricClass SATURATION = fromString("Saturation");

    /**
     * Creates or finds a MetricClass from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding MetricClass.
     */
    @JsonCreator
    public static MetricClass fromString(String name) {
        return fromString(name, MetricClass.class);
    }

    /** @return known MetricClass values. */
    public static Collection<MetricClass> values() {
        return values(MetricClass.class);
    }
}
