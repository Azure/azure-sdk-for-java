// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for VirtualMachineSizeTypes. */
public final class VirtualMachineSizeTypes extends ExpandableStringEnum<VirtualMachineSizeTypes> {
TYPES_PLACE_HOLDER
    /**
     * Creates or finds a VirtualMachineSizeTypes from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding VirtualMachineSizeTypes.
     */
    @JsonCreator
    public static VirtualMachineSizeTypes fromString(String name) {
        return fromString(name, VirtualMachineSizeTypes.class);
    }

    /** @return known VirtualMachineSizeTypes values. */
    public static Collection<VirtualMachineSizeTypes> values() {
        return values(VirtualMachineSizeTypes.class);
    }
}
