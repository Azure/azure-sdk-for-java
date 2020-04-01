/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.arm.ExpandableStringEnum;

import java.util.Collection;

/**
 * Compute resource types.
 */
@Fluent
public class ComputeResourceType  extends ExpandableStringEnum<ComputeResourceType> {
    /**
     * Static value availabilitySets for ComputeResourceType.
     */
    public static final ComputeResourceType AVAILABILITYSETS = fromString("availabilitySets");
    /**
     * Static value disks for ComputeResourceType.
     */
    public static final ComputeResourceType DISKS = fromString("disks");
    /**
     * Static value snapshots for ComputeResourceType.
     */
    public static final ComputeResourceType SNAPSHOTS = fromString("snapshots");
    /**
     * Static value virtualMachines for ComputeResourceType.
     */
    public static final ComputeResourceType VIRTUALMACHINES = fromString("virtualMachines");

    /**
     * Finds or creates compute resource type based on the specified string.
     *
     * @param str the compute resource type in string format
     * @return an instance of ComputeResourceType
     */
    public static ComputeResourceType fromString(String str) {
        return fromString(str, ComputeResourceType.class);
    }

    /**
     * @return known compute resource types
     */
    public static Collection<ComputeResourceType> values() {
        return values(ComputeResourceType.class);
    }
}
