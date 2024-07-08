// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.v2.annotation.Fluent;
import com.azure.core.v2.annotation.JsonFlatten;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Model used for testing {@link JsonFlatten}.
 */
@Fluent
public final class VirtualMachineScaleSetNetworkProfile {
    @JsonProperty(value = "networkInterfaceConfigurations")
    private List<VirtualMachineScaleSetNetworkConfiguration> networkInterfaceConfigurations;

    public VirtualMachineScaleSetNetworkProfile setNetworkInterfaceConfigurations(
        List<VirtualMachineScaleSetNetworkConfiguration> networkInterfaceConfigurations) {
        this.networkInterfaceConfigurations = networkInterfaceConfigurations;
        return this;
    }

    public List<VirtualMachineScaleSetNetworkConfiguration> getNetworkInterfaceConfigurations() {
        return networkInterfaceConfigurations;
    }
}
