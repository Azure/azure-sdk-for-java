// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.core.annotation.Fluent;
import com.azure.security.keyvault.keys.implementation.models.LifetimeActionTrigger;
import com.azure.security.keyvault.keys.implementation.models.LifetimeActionType;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an action that will be performed by Key Vault over the lifetime of a key.
 */
@Fluent
public final class KeyRotationLifetimeAction {
    @JsonProperty(value = "trigger")
    private final LifetimeActionTrigger trigger;

    @JsonProperty(value = "action")
    private final LifetimeActionType actionType;

    /**
     * Creates a {@link KeyRotationLifetimeAction}.
     *
     * @param action The {@link KeyRotationPolicyAction policy action}.
     */
    public KeyRotationLifetimeAction(KeyRotationPolicyAction action) {
        this.actionType = new LifetimeActionType().setType(action);
        this.trigger = new LifetimeActionTrigger();
    }

    /**
     * Get the {@link KeyRotationPolicyAction policy action}.
     *
     * @return The {@link KeyRotationPolicyAction policy action}.
     */
    public KeyRotationPolicyAction getAction() {
        return this.actionType.getType();
    }

    /**
     * Get the time after creation to attempt to rotate in ISO 8601 duration format. For example, 90 days would be
     * "P90D", 3 months would be "P3M" and 1 year and 10 days would be "P1Y10D". See
     * <a href="https://wikipedia.org/wiki/ISO_8601#Durations">Wikipedia</a> for more information on ISO 8601 durations.
     *
     * @return The time after creation to attempt to rotate in ISO duration format.
     */
    public String getTimeAfterCreate() {
        return this.trigger.getTimeAfterCreate();
    }

    /**
     * Set the time after creation to attempt to rotate in ISO 8601 duration format. For example, 90 days would be
     * "P90D", 3 months would be "P3M" and 1 year and 10 days would be "P1Y10D". See
     * <a href="https://wikipedia.org/wiki/ISO_8601#Durations">Wikipedia</a> for more information on ISO 8601 durations.
     *
     * @param timeAfterCreate The time after creation to attempt to rotate in ISO duration format.
     *
     * @return The updated {@link KeyRotationLifetimeAction} object.
     */
    public KeyRotationLifetimeAction setTimeAfterCreate(String timeAfterCreate) {
        this.trigger.setTimeAfterCreate(timeAfterCreate);

        return this;
    }

    /**
     * Get the time before expiry to attempt to rotate or notify in ISO 8601 duration format. For example, 90 days would
     * be "P90D", 3 months would be "P3M" and 1 year and 10 days would be "P1Y10D". See
     * <a href="https://wikipedia.org/wiki/ISO_8601#Durations">Wikipedia</a> for more information on ISO 8601 durations.
     *
     * @return The time before expiry to attempt to rotate or notify in ISO duration format.
     */
    public String getTimeBeforeExpiry() {
        return this.trigger.getTimeBeforeExpiry();
    }

    /**
     * Set the time before expiry to attempt to rotate or notify in ISO 8601 duration format. For example, 90 days would
     * be "P90D", 3 months would be "P3M" and 1 year and 10 days would be "P1Y10D". See
     * <a href="https://wikipedia.org/wiki/ISO_8601#Durations">Wikipedia</a> for more information on ISO 8601 durations.
     *
     * @param timeBeforeExpiry The time before expiry to attempt to rotate or notify in ISO duration format.
     *
     * @return The updated {@link KeyRotationLifetimeAction} object.
     */
    public KeyRotationLifetimeAction setTimeBeforeExpiry(String timeBeforeExpiry) {
        this.trigger.setTimeBeforeExpiry(timeBeforeExpiry);

        return this;
    }
}
