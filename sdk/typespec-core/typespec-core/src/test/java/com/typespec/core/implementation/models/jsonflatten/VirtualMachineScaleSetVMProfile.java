// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.models.jsonflatten;

import com.typespec.core.annotation.Fluent;
import com.typespec.core.annotation.JsonFlatten;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model used for testing {@link JsonFlatten}.
 */
@Fluent
public final class VirtualMachineScaleSetVMProfile {
    @JsonProperty(value = "networkProfile")
    private VirtualMachineScaleSetNetworkProfile networkProfile;

    public VirtualMachineScaleSetVMProfile setNetworkProfile(VirtualMachineScaleSetNetworkProfile networkProfile) {
        this.networkProfile = networkProfile;
        return this;
    }

    public VirtualMachineScaleSetNetworkProfile getNetworkProfile() {
        return networkProfile;
    }
}
