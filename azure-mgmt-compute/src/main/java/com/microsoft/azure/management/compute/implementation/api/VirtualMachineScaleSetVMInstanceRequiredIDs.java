/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Specifies the list of virtual machine scale set instance IDs.
 */
public class VirtualMachineScaleSetVMInstanceRequiredIDs {
    /**
     * Gets or sets the virtual machine scale set instance ids.
     */
    @JsonProperty(required = true)
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
     * @return the VirtualMachineScaleSetVMInstanceRequiredIDs object itself.
     */
    public VirtualMachineScaleSetVMInstanceRequiredIDs withInstanceIds(List<String> instanceIds) {
        this.instanceIds = instanceIds;
        return this;
    }

}
