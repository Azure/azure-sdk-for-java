/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Capture Virtual Machine parameters.
 */
public class VirtualMachineCaptureParametersInner {
    /**
     * Gets or sets the captured VirtualHardDisk's name prefix.
     */
    @JsonProperty(required = true)
    private String vhdPrefix;

    /**
     * Gets or sets the destination container name.
     */
    @JsonProperty(required = true)
    private String destinationContainerName;

    /**
     * Gets or sets whether it overwrites destination VirtualHardDisk if true,
     * in case of conflict.
     */
    @JsonProperty(required = true)
    private boolean overwriteVhds;

    /**
     * Get the vhdPrefix value.
     *
     * @return the vhdPrefix value
     */
    public String vhdPrefix() {
        return this.vhdPrefix;
    }

    /**
     * Set the vhdPrefix value.
     *
     * @param vhdPrefix the vhdPrefix value to set
     * @return the VirtualMachineCaptureParametersInner object itself.
     */
    public VirtualMachineCaptureParametersInner withVhdPrefix(String vhdPrefix) {
        this.vhdPrefix = vhdPrefix;
        return this;
    }

    /**
     * Get the destinationContainerName value.
     *
     * @return the destinationContainerName value
     */
    public String destinationContainerName() {
        return this.destinationContainerName;
    }

    /**
     * Set the destinationContainerName value.
     *
     * @param destinationContainerName the destinationContainerName value to set
     * @return the VirtualMachineCaptureParametersInner object itself.
     */
    public VirtualMachineCaptureParametersInner withDestinationContainerName(String destinationContainerName) {
        this.destinationContainerName = destinationContainerName;
        return this;
    }

    /**
     * Get the overwriteVhds value.
     *
     * @return the overwriteVhds value
     */
    public boolean overwriteVhds() {
        return this.overwriteVhds;
    }

    /**
     * Set the overwriteVhds value.
     *
     * @param overwriteVhds the overwriteVhds value to set
     * @return the VirtualMachineCaptureParametersInner object itself.
     */
    public VirtualMachineCaptureParametersInner withOverwriteVhds(boolean overwriteVhds) {
        this.overwriteVhds = overwriteVhds;
        return this;
    }

}
