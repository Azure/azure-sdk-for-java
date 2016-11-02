/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

/**
 * Netowrk usage units.
 */
public class NetworkUsageUnit {
    /** Static value Count for NetworkUsageUnit. */
    public static final NetworkUsageUnit COUNT = new NetworkUsageUnit("Count");

    /** Static value Bytes for NetworkUsageUnit. */
    public static final NetworkUsageUnit BYTES = new NetworkUsageUnit("Bytes");

    /** Static value Seconds for NetworkUsageUnit. */
    public static final NetworkUsageUnit SECONDS = new NetworkUsageUnit("Seconds");

    /** Static value Percent for NetworkUsageUnit. */
    public static final NetworkUsageUnit PERCENT = new NetworkUsageUnit("Percent");

    /** Static value CountsPerSecond for NetworkUsageUnit. */
    public static final NetworkUsageUnit COUNTS_PER_SECOND = new NetworkUsageUnit("CountsPerSecond");

    /** Static value BytesPerSecond for ComputeUsageUnit. */
    public static final NetworkUsageUnit BYTES_PER_SECOND = new NetworkUsageUnit("BytesPerSecond");

    /**
     * The string value of the network usage unit.
     */
    private final String value;

    /**
     * Creates a custom value for NetworkUsageUnit.
     * @param value the custom value
     */
    public NetworkUsageUnit(String value) {
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
        if (!(obj instanceof NetworkUsageUnit)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        NetworkUsageUnit rhs = (NetworkUsageUnit) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equals(rhs.value);
        }
    }
}
