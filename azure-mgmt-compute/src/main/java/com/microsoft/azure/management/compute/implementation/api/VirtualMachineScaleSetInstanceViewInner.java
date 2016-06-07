/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The instance view of a virtual machine scale set.
 */
public class VirtualMachineScaleSetInstanceViewInner {
    /**
     * Gets the instance view status summary for the virtual machine scale set.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private VirtualMachineScaleSetInstanceViewStatusesSummary virtualMachine;

    /**
     * Gets the extensions information.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<VirtualMachineScaleSetVMExtensionsSummary> extensions;

    /**
     * Gets or sets the resource status information.
     */
    private List<InstanceViewStatus> statuses;

    /**
     * Get the virtualMachine value.
     *
     * @return the virtualMachine value
     */
    public VirtualMachineScaleSetInstanceViewStatusesSummary virtualMachine() {
        return this.virtualMachine;
    }

    /**
     * Get the extensions value.
     *
     * @return the extensions value
     */
    public List<VirtualMachineScaleSetVMExtensionsSummary> extensions() {
        return this.extensions;
    }

    /**
     * Get the statuses value.
     *
     * @return the statuses value
     */
    public List<InstanceViewStatus> statuses() {
        return this.statuses;
    }

    /**
     * Set the statuses value.
     *
     * @param statuses the statuses value to set
     * @return the VirtualMachineScaleSetInstanceViewInner object itself.
     */
    public VirtualMachineScaleSetInstanceViewInner withStatuses(List<InstanceViewStatus> statuses) {
        this.statuses = statuses;
        return this;
    }

}
