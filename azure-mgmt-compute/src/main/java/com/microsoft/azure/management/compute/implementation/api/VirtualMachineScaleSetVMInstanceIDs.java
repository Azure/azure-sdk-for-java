/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import java.util.List;

/**
 * Specifies the list of virtual machine scale set instance IDs.
 */
public class VirtualMachineScaleSetVMInstanceIDs {
    /**
     * Gets or sets the virtual machine scale set instance ids.
     */
    private List<String> instanceIds;

    /**
     * Get the instanceIds value.
     *
     * @return the instanceIds value
     */
    public List<String> instanceIds() {
        return this.instanceIds;
    }

    /**
     * Set the instanceIds value.
     *
     * @param instanceIds the instanceIds value to set
     * @return the VirtualMachineScaleSetVMInstanceIDs object itself.
     */
    public VirtualMachineScaleSetVMInstanceIDs withInstanceIds(List<String> instanceIds) {
        this.instanceIds = instanceIds;
        return this;
    }

}
