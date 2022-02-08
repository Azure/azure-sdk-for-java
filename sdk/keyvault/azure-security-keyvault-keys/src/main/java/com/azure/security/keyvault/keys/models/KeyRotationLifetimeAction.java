// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.core.annotation.Fluent;

/**
 * Represents an action that will be performed by Key Vault over the lifetime of a key.
 */
@Fluent
public final class KeyRotationLifetimeAction {
    private final KeyRotationPolicyAction action;
    private String timeAfterCreate;
    private String timeBeforeExpiry;

    /**
     * Creates a {@link KeyRotationLifetimeAction}.
     *
     * @param action The {@link KeyRotationPolicyAction policy action}.
     */
    public KeyRotationLifetimeAction(KeyRotationPolicyAction action) {
        this.action = action;
    }

    /**
     * Get the {@link KeyRotationPolicyAction policy action}.
     *
     * @return The {@link KeyRotationPolicyAction policy action}.
     */
    public KeyRotationPolicyAction getAction() {
        return this.action;
    }

    /**
     * Get the time after creation to attempt to rotate in ISO 8601 duration format. For example, 90 days is represented
     * as follows: "P90D".
     *
     * @return The time after creation to attempt to rotate in ISO duration format.
     */
    public String getTimeAfterCreate() {
        return this.timeAfterCreate;
    }

    /**
     * Set the time after creation to attempt to rotate in ISO 8601 duration format. For example, 90 days is represented
     * as follows: "P90D".
     *
     * @param timeAfterCreate The time after creation to attempt to rotate in ISO duration format.
     *
     * @return The updated {@link KeyRotationLifetimeAction} object.
     */
    public KeyRotationLifetimeAction setTimeAfterCreate(String timeAfterCreate) {
        this.timeAfterCreate = timeAfterCreate;

        return this;
    }

    /**
     * Get the time before expiry to attempt to rotate or notify in ISO 8601 duration format. For example, 90 days is
     * represented as follows: "P90D".
     *
     * @return The time before expiry to attempt to rotate or notify in ISO duration format.
     */
    public String getTimeBeforeExpiry() {
        return this.timeBeforeExpiry;
    }

    /**
     * Set the time before expiry to attempt to rotate or notify in ISO 8601 duration format. For example, 90 days is
     * represented as follows: "P90D".
     *
     * @param timeBeforeExpiry The time before expiry to attempt to rotate or notify in ISO duration format.
     *
     * @return The updated {@link KeyRotationLifetimeAction} object.
     */
    public KeyRotationLifetimeAction setTimeBeforeExpiry(String timeBeforeExpiry) {
        this.timeBeforeExpiry = timeBeforeExpiry;

        return this;
    }
}
