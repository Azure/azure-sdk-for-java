// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.JsonFlatten;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Model used for testing {@link JsonFlatten}.
 */
@Fluent
public final class VirtualMachineIdentity {

    @JsonProperty(value = "type")
    private List<String> type;

    @JsonProperty(value = "userAssignedIdentities")
    private Map<String, Object> userAssignedIdentities;

    public List<String> getType() {
        return type;
    }

    public VirtualMachineIdentity setType(List<String> type) {
        this.type = type;
        return this;
    }

    public Map<String, Object> getUserAssignedIdentities() {
        return userAssignedIdentities;
    }

    public VirtualMachineIdentity setUserAssignedIdentities(
        Map<String, Object> userAssignedIdentities) {
        this.userAssignedIdentities = userAssignedIdentities;
        return this;
    }
}
