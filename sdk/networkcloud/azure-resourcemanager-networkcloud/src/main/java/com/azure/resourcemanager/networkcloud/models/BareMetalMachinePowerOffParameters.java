// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.networkcloud.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** BareMetalMachinePowerOffParameters represents the body of the request to power off bare metal machine. */
@Fluent
public final class BareMetalMachinePowerOffParameters {
    /*
     * The indicator of whether to skip the graceful OS shutdown and power off the bare metal machine immediately.
     */
    @JsonProperty(value = "skipShutdown")
    private BareMetalMachineSkipShutdown skipShutdown;

    /** Creates an instance of BareMetalMachinePowerOffParameters class. */
    public BareMetalMachinePowerOffParameters() {
    }

    /**
     * Get the skipShutdown property: The indicator of whether to skip the graceful OS shutdown and power off the bare
     * metal machine immediately.
     *
     * @return the skipShutdown value.
     */
    public BareMetalMachineSkipShutdown skipShutdown() {
        return this.skipShutdown;
    }

    /**
     * Set the skipShutdown property: The indicator of whether to skip the graceful OS shutdown and power off the bare
     * metal machine immediately.
     *
     * @param skipShutdown the skipShutdown value to set.
     * @return the BareMetalMachinePowerOffParameters object itself.
     */
    public BareMetalMachinePowerOffParameters withSkipShutdown(BareMetalMachineSkipShutdown skipShutdown) {
        this.skipShutdown = skipShutdown;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
    }
}
