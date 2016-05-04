/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The configuration of virtual machines for a pool.
 */
public class VirtualMachineConfiguration {
    /**
     * Gets or sets information about the platform or marketplace image to use.
     */
    @JsonProperty(required = true)
    private ImageReference imageReference;

    /**
     * Gets or sets the SKU of Batch Node Agent that needs to be provisioned
     * on the compute node. This property must match the ImageReference
     * property.
     */
    private String nodeAgentSKUId;

    /**
     * Gets or sets Windows operating system settings on the virtual machine.
     * This property must not be specified if the ImageReference property
     * referencs a Linux OS image.
     */
    private WindowsConfiguration windowsConfiguration;

    /**
     * Get the imageReference value.
     *
     * @return the imageReference value
     */
    public ImageReference getImageReference() {
        return this.imageReference;
    }

    /**
     * Set the imageReference value.
     *
     * @param imageReference the imageReference value to set
     */
    public void setImageReference(ImageReference imageReference) {
        this.imageReference = imageReference;
    }

    /**
     * Get the nodeAgentSKUId value.
     *
     * @return the nodeAgentSKUId value
     */
    public String getNodeAgentSKUId() {
        return this.nodeAgentSKUId;
    }

    /**
     * Set the nodeAgentSKUId value.
     *
     * @param nodeAgentSKUId the nodeAgentSKUId value to set
     */
    public void setNodeAgentSKUId(String nodeAgentSKUId) {
        this.nodeAgentSKUId = nodeAgentSKUId;
    }

    /**
     * Get the windowsConfiguration value.
     *
     * @return the windowsConfiguration value
     */
    public WindowsConfiguration getWindowsConfiguration() {
        return this.windowsConfiguration;
    }

    /**
     * Set the windowsConfiguration value.
     *
     * @param windowsConfiguration the windowsConfiguration value to set
     */
    public void setWindowsConfiguration(WindowsConfiguration windowsConfiguration) {
        this.windowsConfiguration = windowsConfiguration;
    }

}
