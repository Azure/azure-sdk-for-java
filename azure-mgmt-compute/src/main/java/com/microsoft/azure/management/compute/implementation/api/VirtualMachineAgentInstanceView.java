/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import java.util.List;

/**
 * The instance view of the VM Agent running on the virtual machine.
 */
public class VirtualMachineAgentInstanceView {
    /**
     * Gets or sets the VM Agent full version.
     */
    private String vmAgentVersion;

    /**
     * Gets or sets the virtual machine extension handler instance view.
     */
    private List<VirtualMachineExtensionHandlerInstanceView> extensionHandlers;

    /**
     * Gets or sets the resource status information.
     */
    private List<InstanceViewStatus> statuses;

    /**
     * Get the vmAgentVersion value.
     *
     * @return the vmAgentVersion value
     */
    public String vmAgentVersion() {
        return this.vmAgentVersion;
    }

    /**
     * Set the vmAgentVersion value.
     *
     * @param vmAgentVersion the vmAgentVersion value to set
     * @return the VirtualMachineAgentInstanceView object itself.
     */
    public VirtualMachineAgentInstanceView withVmAgentVersion(String vmAgentVersion) {
        this.vmAgentVersion = vmAgentVersion;
        return this;
    }

    /**
     * Get the extensionHandlers value.
     *
     * @return the extensionHandlers value
     */
    public List<VirtualMachineExtensionHandlerInstanceView> extensionHandlers() {
        return this.extensionHandlers;
    }

    /**
     * Set the extensionHandlers value.
     *
     * @param extensionHandlers the extensionHandlers value to set
     * @return the VirtualMachineAgentInstanceView object itself.
     */
    public VirtualMachineAgentInstanceView withExtensionHandlers(List<VirtualMachineExtensionHandlerInstanceView> extensionHandlers) {
        this.extensionHandlers = extensionHandlers;
        return this;
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
     * @return the VirtualMachineAgentInstanceView object itself.
     */
    public VirtualMachineAgentInstanceView withStatuses(List<InstanceViewStatus> statuses) {
        this.statuses = statuses;
        return this;
    }

}
