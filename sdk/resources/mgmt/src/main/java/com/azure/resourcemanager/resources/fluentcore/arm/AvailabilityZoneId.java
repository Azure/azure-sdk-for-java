// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Availability zone identifiers.
 */
public class AvailabilityZoneId extends ExpandableStringEnum<AvailabilityZoneId> {
    /**
     * Static value 1 for AvailabilityZoneId.
     */
    public static final AvailabilityZoneId ZONE_1 = fromString("1");

    /**
     * Static value 2 for AvailabilityZoneId.
     */
    public static final AvailabilityZoneId ZONE_2 = fromString("2");

    /**
     * Static value 3 for AvailabilityZoneId.
     */
    public static final AvailabilityZoneId ZONE_3 = fromString("3");

    /**
     * Finds or creates an availability zone identifier based on the specified identifier in string format.
     *
     * @param id the zone identifier in string format
     * @return an instance of AvailabilityZone
     */
    public static AvailabilityZoneId fromString(String id) {
        return fromString(id, AvailabilityZoneId.class);
    }

    /**
     * @return known availability zone identifiers
     */
    public static Collection<AvailabilityZoneId> values() {
        return values(AvailabilityZoneId.class);
    }
}
