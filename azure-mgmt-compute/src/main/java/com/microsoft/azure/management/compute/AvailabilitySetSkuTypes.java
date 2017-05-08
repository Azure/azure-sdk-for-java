/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import java.util.Collection;

import com.microsoft.azure.management.resources.fluentcore.arm.ExpandableStringEnum;

/**
 * Defines values for AvailabilitySetSkuTypes.
 */
// TODO: This should be called AvailabilitySetSkuType
public final class AvailabilitySetSkuTypes extends ExpandableStringEnum<AvailabilitySetSkuTypes> {
    /** Static value Aligned for AvailabilitySetSkuTypes. */
    public static final AvailabilitySetSkuTypes MANAGED = fromString("Aligned");

    /** Static value Classic for AvailabilitySetSkuTypes. */
    public static final AvailabilitySetSkuTypes UNMANAGED = fromString("Classic");

    /**
     * Creates or finds an availability set SKU type baes on its name.
     * @param name a name
     * @return an availability set SKU type
     */
    public static AvailabilitySetSkuTypes fromString(String name) {
        return fromString(name, AvailabilitySetSkuTypes.class);
    }

    /**
     * @return known availability set sku types
     */
    public static Collection<AvailabilitySetSkuTypes> values() {
        return values(AvailabilitySetSkuTypes.class);
    }
}
