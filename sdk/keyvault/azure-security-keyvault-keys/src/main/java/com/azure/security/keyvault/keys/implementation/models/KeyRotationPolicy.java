// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents the rotation policy for a key.
 */
@Fluent
public final class KeyRotationPolicy {
    @JsonProperty(value = "id", access = JsonProperty.Access.WRITE_ONLY)
    private String id;

    @JsonProperty(value = "lifetimeActions")
    private List<LifetimeAction> lifetimeActions;

    @JsonProperty(value = "attributes")
    private KeyRotationPolicyAttributes attributes;

    /**
     * Get the {@link KeyRotationPolicy policy} id.
     *
     * @return The {@link KeyRotationPolicy policy} id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the actions that will be performed by Key Vault over the lifetime of a key.
     *
     * @return The {@link LifetimeAction actions} in this {@link KeyRotationPolicy policy}.
     */
    public List<LifetimeAction> getLifetimeActions() {
        return this.lifetimeActions;
    }

    /**
     * Set the actions that will be performed by Key Vault over the lifetime of a key.
     *
     * @param lifetimeActions The {@link LifetimeAction actions} to set.
     *
     * @return The updated {@link KeyRotationPolicy} object.
     */
    public KeyRotationPolicy setLifetimeActions(List<LifetimeAction> lifetimeActions) {
        this.lifetimeActions = lifetimeActions;

        return this;
    }

    /**
     * Get the {@link KeyRotationPolicyAttributes policy attributes}.
     *
     * @return The {@link KeyRotationPolicyAttributes policy attributes}.
     */
    public KeyRotationPolicyAttributes getAttributes() {
        return this.attributes;
    }

    /**
     * Set the {@link KeyRotationPolicyAttributes policy attributes}.
     *
     * @param attributes The {@link KeyRotationPolicyAttributes policy attributes} to set.
     *
     * @return The updated {@link KeyRotationPolicy} object.
     */
    public KeyRotationPolicy setAttributes(KeyRotationPolicyAttributes attributes) {
        this.attributes = attributes;

        return this;
    }
}
