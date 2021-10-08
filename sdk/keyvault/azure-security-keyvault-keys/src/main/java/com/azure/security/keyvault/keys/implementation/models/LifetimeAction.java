// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an action that will be performed by Key Vault over the lifetime of a key.
 */
@Fluent
public final class LifetimeAction {
    @JsonProperty(value = "trigger")
    private LifetimeActionTrigger trigger;

    @JsonProperty(value = "action")
    private LifetimeActionsType action;

    /**
     * Get the {@link LifetimeActionTrigger action trigger}, the condition that will execute the action.
     *
     * @return The {@link LifetimeActionTrigger action trigger}.
     */
    public LifetimeActionTrigger getTrigger() {
        return this.trigger;
    }

    /**
     * Set the {@link LifetimeActionTrigger action trigger}, the condition that will execute the action.
     *
     * @param trigger The {@link LifetimeActionTrigger action trigger} to set.
     *
     * @return The updated {@link LifetimeAction} object.
     */
    public LifetimeAction setTrigger(LifetimeActionTrigger trigger) {
        this.trigger = trigger;

        return this;
    }

    /**
     * Get the type of the action that will be executed.
     *
     * @return The {@link LifetimeActionsType action type}.
     */
    public LifetimeActionsType getAction() {
        return this.action;
    }

    /**
     * Set the type of the action that will be executed.
     *
     * @param action The {@link LifetimeActionsType action type} to set.
     *
     * @return The updated {@link LifetimeAction} object.
     */
    public LifetimeAction setAction(LifetimeActionsType action) {
        this.action = action;

        return this;
    }
}
