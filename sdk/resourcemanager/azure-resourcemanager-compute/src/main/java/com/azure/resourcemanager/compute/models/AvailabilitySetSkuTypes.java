// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for AvailabilitySetSkuTypes. */
public final class AvailabilitySetSkuTypes extends ExpandableStringEnum<AvailabilitySetSkuTypes> {
    /** Static value Classic for AvailabilitySetSkuTypes. */
    public static final AvailabilitySetSkuTypes CLASSIC = fromString("Classic");

    /** Static value Aligned for AvailabilitySetSkuTypes. */
    public static final AvailabilitySetSkuTypes ALIGNED = fromString("Aligned");

    /**
     * Creates or finds a AvailabilitySetSkuTypes from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding AvailabilitySetSkuTypes.
     */
    @JsonCreator
    public static AvailabilitySetSkuTypes fromString(String name) {
        return fromString(name, AvailabilitySetSkuTypes.class);
    }

    /** @return known AvailabilitySetSkuTypes values. */
    public static Collection<AvailabilitySetSkuTypes> values() {
        return values(AvailabilitySetSkuTypes.class);
    }
}
