// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for MetricUnit. */
public final class MetricsUnit extends ExpandableStringEnum<MetricsUnit> {
    /** Static value Count for MetricUnit. */
    public static final MetricsUnit COUNT = fromString("Count");

    /** Static value Bytes for MetricUnit. */
    public static final MetricsUnit BYTES = fromString("Bytes");

    /** Static value Seconds for MetricUnit. */
    public static final MetricsUnit SECONDS = fromString("Seconds");

    /** Static value CountPerSecond for MetricUnit. */
    public static final MetricsUnit COUNT_PER_SECOND = fromString("CountPerSecond");

    /** Static value BytesPerSecond for MetricUnit. */
    public static final MetricsUnit BYTES_PER_SECOND = fromString("BytesPerSecond");

    /** Static value Percent for MetricUnit. */
    public static final MetricsUnit PERCENT = fromString("Percent");

    /** Static value MilliSeconds for MetricUnit. */
    public static final MetricsUnit MILLI_SECONDS = fromString("MilliSeconds");

    /** Static value ByteSeconds for MetricUnit. */
    public static final MetricsUnit BYTE_SECONDS = fromString("ByteSeconds");

    /** Static value Unspecified for MetricUnit. */
    public static final MetricsUnit UNSPECIFIED = fromString("Unspecified");

    /** Static value Cores for MetricUnit. */
    public static final MetricsUnit CORES = fromString("Cores");

    /** Static value MilliCores for MetricUnit. */
    public static final MetricsUnit MILLI_CORES = fromString("MilliCores");

    /** Static value NanoCores for MetricUnit. */
    public static final MetricsUnit NANO_CORES = fromString("NanoCores");

    /** Static value BitsPerSecond for MetricUnit. */
    public static final MetricsUnit BITS_PER_SECOND = fromString("BitsPerSecond");

    /**
     * Creates or finds a MetricUnit from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding MetricUnit.
     */
    @JsonCreator
    public static MetricsUnit fromString(String name) {
        return fromString(name, MetricsUnit.class);
    }

    /** @return known MetricUnit values. */
    public static Collection<MetricsUnit> values() {
        return values(MetricsUnit.class);
    }
}
