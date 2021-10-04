// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.security.keyvault.keys.models.KeyRotationPolicyAction;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The type of the action that will be executed.
 */
@Fluent
public final class LifetimeActionsType {
    @JsonProperty(value = "type")
    private KeyRotationPolicyAction type;

    /**
     * Get the {@link KeyRotationPolicyAction type} of the action.
     *
     * @return The {@link KeyRotationPolicyAction type} of the action.
     */
    public KeyRotationPolicyAction getType() {
        return this.type;
    }

    /**
     * Set the {@link KeyRotationPolicyAction type} of the action.
     *
     * @param type The {@link KeyRotationPolicyAction type} to set.
     *
     * @return The updated {@link LifetimeActionsType} object.
     */
    public LifetimeActionsType setType(KeyRotationPolicyAction type) {
        this.type = type;

        return this;
    }
}
