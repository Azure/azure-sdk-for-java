/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The configuration for compute nodes in a pool based on the Azure Virtual
 * Machines infrastructure.
 */
public class VirtualMachineConfiguration {
    /**
     * A reference to the Azure Virtual Machines Marketplace image to use.
     */
    @JsonProperty(required = true)
    private ImageReference imageReference;

    /**
     * The SKU of Batch Node Agent to be provisioned on the compute node. The
     * Batch node agent is a program that runs on each node in the pool, and
     * provides the command-and-control interface between the node and the
     * Batch service. There are different implementations of the node agent,
     * known as SKUs, for different operating systems.
     */
    @JsonProperty(required = true)
    private String nodeAgentSKUId;

    /**
     * Windows operating system settings on the virtual machine. This property
     * must not be specified if the ImageReference property specifies a Linux
     * OS image.
     */
    private WindowsConfiguration windowsConfiguration;

    /**
     * Get the imageReference value.
     *
     * @return the imageReference value
     */
    public ImageReference imageReference() {
        return this.imageReference;
    }

    /**
     * Set the imageReference value.
     *
     * @param imageReference the imageReference value to set
     * @return the VirtualMachineConfiguration object itself.
     */
    public VirtualMachineConfiguration withImageReference(ImageReference imageReference) {
        this.imageReference = imageReference;
        return this;
    }

    /**
     * Get the nodeAgentSKUId value.
     *
     * @return the nodeAgentSKUId value
     */
    public String nodeAgentSKUId() {
        return this.nodeAgentSKUId;
    }

    /**
     * Set the nodeAgentSKUId value.
     *
     * @param nodeAgentSKUId the nodeAgentSKUId value to set
     * @return the VirtualMachineConfiguration object itself.
     */
    public VirtualMachineConfiguration withNodeAgentSKUId(String nodeAgentSKUId) {
        this.nodeAgentSKUId = nodeAgentSKUId;
        return this;
    }

    /**
     * Get the windowsConfiguration value.
     *
     * @return the windowsConfiguration value
     */
    public WindowsConfiguration windowsConfiguration() {
        return this.windowsConfiguration;
    }

    /**
     * Set the windowsConfiguration value.
     *
     * @param windowsConfiguration the windowsConfiguration value to set
     * @return the VirtualMachineConfiguration object itself.
     */
    public VirtualMachineConfiguration withWindowsConfiguration(WindowsConfiguration windowsConfiguration) {
        this.windowsConfiguration = windowsConfiguration;
        return this;
    }

}
