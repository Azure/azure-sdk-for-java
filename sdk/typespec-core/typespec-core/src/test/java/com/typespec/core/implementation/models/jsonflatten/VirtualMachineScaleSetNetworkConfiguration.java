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
public final class VirtualMachineScaleSetNetworkConfiguration {
    @JsonProperty(value = "name")
    private String name;

    @JsonFlatten
    @JsonProperty(value = "properties.primary")
    private Boolean primary;

    public VirtualMachineScaleSetNetworkConfiguration setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public VirtualMachineScaleSetNetworkConfiguration setPrimary(Boolean primary) {
        this.primary = primary;
        return this;
    }

    public Boolean getPrimary() {
        return primary;
    }
}
