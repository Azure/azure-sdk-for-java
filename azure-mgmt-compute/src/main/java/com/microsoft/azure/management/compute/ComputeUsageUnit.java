/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute;

/**
 * Compute usage units.
 */
public class ComputeUsageUnit {
    /** Static value Count for ComputeUsageUnit. */
    public static final ComputeUsageUnit COUNT = new ComputeUsageUnit("Count");

    /** Static value Bytes for ComputeUsageUnit. */
    public static final ComputeUsageUnit BYTES = new ComputeUsageUnit("Bytes");

    /** Static value Seconds for ComputeUsageUnit. */
    public static final ComputeUsageUnit SECONDS = new ComputeUsageUnit("Seconds");

    /** Static value Percent for ComputeUsageUnit. */
    public static final ComputeUsageUnit PERCENT = new ComputeUsageUnit("Percent");

    /** Static value CountsPerSecond for ComputeUsageUnit. */
    public static final ComputeUsageUnit COUNTS_PER_SECOND = new ComputeUsageUnit("CountsPerSecond");

    /** Static value BytesPerSecond for ComputeUsageUnit. */
    public static final ComputeUsageUnit BYTES_PER_SECOND = new ComputeUsageUnit("BytesPerSecond");

    /**
     * The string value of the compute usage unit.
     */
    private final String value;

    /**
     * Creates a custom value for ComputeUsageUnit.
     * @param value the custom value
     */
    public ComputeUsageUnit(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        String value = this.toString();
        if (!(obj instanceof ComputeUsageUnit)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        ComputeUsageUnit rhs = (ComputeUsageUnit) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equals(rhs.value);
        }
    }
}
