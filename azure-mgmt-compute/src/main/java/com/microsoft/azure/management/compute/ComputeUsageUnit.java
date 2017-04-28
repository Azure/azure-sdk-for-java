/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Compute usage units.
 */
public class ComputeUsageUnit {
    // This needs to be at the beginning for the initialization to happen correctly
    private static final Map<String, ComputeUsageUnit> VALUES_BY_NAME = new HashMap<>();

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
     * @return predefined compute usage units
     */
    public static ComputeUsageUnit[] values() {
        Collection<ComputeUsageUnit> valuesCollection = VALUES_BY_NAME.values();
        return valuesCollection.toArray(new ComputeUsageUnit[valuesCollection.size()]);
    }

    /**
     * Creates a custom value for ComputeUsageUnit.
     * @param value the custom value
     */
    public ComputeUsageUnit(String value) {
        // TODO: This constructor should be private, but keeping as is for now to keep 1.0.0 back compat
        this.value = value;
        VALUES_BY_NAME.put(value.toLowerCase(), this);
    }

    @Override
    public String toString() {
        return this.value;
    }

    /**
     * Parses a value into a compute usage unit and creates a new ComputeUsageUnit instance if not found among the existing ones.
     *
     * @param value a compute usage unit name
     * @return the parsed or created compute usage unit
     */
    public static ComputeUsageUnit fromString(String value) {
        if (value == null) {
            return null;
        }

        ComputeUsageUnit result = VALUES_BY_NAME.get(value.toLowerCase());
        if (result != null) {
            return result;
        } else {
            return new ComputeUsageUnit(value);
        }
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
        } else  if (obj == this) {
            return true;
        } else if (value == null) {
            return ((ComputeUsageUnit) obj).value == null;
        } else {
            return value.equals(((ComputeUsageUnit) obj).value);
        }
    }
}
