// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Compute usage units. */
public final class ComputeUsageUnit extends ExpandableStringEnum<ComputeUsageUnit> {
    /** Static value Count for ComputeUsageUnit. */
    public static final ComputeUsageUnit COUNT = fromString("Count");

    /** Static value Bytes for ComputeUsageUnit. */
    public static final ComputeUsageUnit BYTES = fromString("Bytes");

    /** Static value Seconds for ComputeUsageUnit. */
    public static final ComputeUsageUnit SECONDS = fromString("Seconds");

    /** Static value Percent for ComputeUsageUnit. */
    public static final ComputeUsageUnit PERCENT = fromString("Percent");

    /** Static value CountsPerSecond for ComputeUsageUnit. */
    public static final ComputeUsageUnit COUNTS_PER_SECOND = fromString("CountsPerSecond");

    /** Static value BytesPerSecond for ComputeUsageUnit. */
    public static final ComputeUsageUnit BYTES_PER_SECOND = fromString("BytesPerSecond");

    /**
     * Creates or finds a compute usage unit based on its name.
     *
     * @param name a name
     * @return a corresponding ComputeUsageUnit
     */
    public static ComputeUsageUnit fromString(String name) {
        return fromString(name, ComputeUsageUnit.class);
    }

    /** @return known compute usage units */
    public Collection<ComputeUsageUnit> values() {
        return values(ComputeUsageUnit.class);
    }
}
