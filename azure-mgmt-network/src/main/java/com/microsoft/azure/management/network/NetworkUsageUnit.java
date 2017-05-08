/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import java.util.Collection;

import com.microsoft.azure.management.resources.fluentcore.arm.ExpandableStringEnum;

/**
 * Netowork usage units.
 */
public class NetworkUsageUnit extends ExpandableStringEnum<NetworkUsageUnit> {
    /** Static value Count for NetworkUsageUnit. */
    public static final NetworkUsageUnit COUNT = fromString("Count");

    /** Static value Bytes for NetworkUsageUnit. */
    public static final NetworkUsageUnit BYTES = fromString("Bytes");

    /** Static value Seconds for NetworkUsageUnit. */
    public static final NetworkUsageUnit SECONDS = fromString("Seconds");

    /** Static value Percent for NetworkUsageUnit. */
    public static final NetworkUsageUnit PERCENT = fromString("Percent");

    /** Static value CountsPerSecond for NetworkUsageUnit. */
    public static final NetworkUsageUnit COUNTS_PER_SECOND = fromString("CountsPerSecond");

    /** Static value BytesPerSecond for ComputeUsageUnit. */
    public static final NetworkUsageUnit BYTES_PER_SECOND = fromString("BytesPerSecond");

    /**
     * Finds or creates a network usage unit based on the specified name.
     * @param name a name
     * @return an instance of NetworkUsageUnit
     */
    public static NetworkUsageUnit fromString(String name) {
        return fromString(name, NetworkUsageUnit.class);
    }

    /**
     * @return known network usage units
     */
    public static Collection<NetworkUsageUnit> values() {
        return values(NetworkUsageUnit.class);
    }
}
