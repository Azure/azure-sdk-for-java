/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute;

import java.util.Collection;

import com.microsoft.azure.management.resources.fluentcore.arm.ExpandableStringEnum;

/**
 * Compute usage units.
 */
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
     * @param name a name
     * @return a corresponding ComputeUsageUnit
     */
    public static ComputeUsageUnit fromString(String name) {
        return fromString(name, ComputeUsageUnit.class);
    }

    /**
     * @return known compute usage units
     */
    public Collection<ComputeUsageUnit> values() {
        return values(ComputeUsageUnit.class);
    }
}
