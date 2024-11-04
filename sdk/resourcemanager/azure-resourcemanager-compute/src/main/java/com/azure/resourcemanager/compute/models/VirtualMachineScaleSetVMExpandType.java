// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Expand type for virtual machine in virtual machine scale set.
 */
public class VirtualMachineScaleSetVMExpandType extends ExpandableStringEnum<VirtualMachineScaleSetVMExpandType> {

    /** Static value 'instanceView' for VirtualMachineScaleSetVMExpandType. */
    public static final VirtualMachineScaleSetVMExpandType INSTANCE_VIEW = fromString("instanceView");

    /**
     * Creates or finds a VirtualMachineScaleSetVMExpandType from its string representation.
     * @param name a name to look for
     * @return the corresponding VirtualMachineScaleSetVMExpandType
     */
    public static VirtualMachineScaleSetVMExpandType fromString(String name) {
        return fromString(name, VirtualMachineScaleSetVMExpandType.class);
    }

    /**
     * @return known VirtualMachineScaleSetVMExpandType values
     */
    public static Collection<VirtualMachineScaleSetVMExpandType> values() {
        return values(VirtualMachineScaleSetVMExpandType.class);
    }
}
