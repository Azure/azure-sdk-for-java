// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.core.annotation.Fluent;

/**
 * Represents an action that will be performed by Key Vault over the lifetime of a key.
 */
@Fluent
public final class KeyRotationLifetimeAction {
    private final KeyRotationPolicyAction type;
    private String timeAfterCreate;
    private String timeBeforeExpiry;

    /**
     * Creates a {@link KeyRotationLifetimeAction}.
     *
     * @param type The {@link KeyRotationPolicyAction type} of the action.
     */
    public KeyRotationLifetimeAction(KeyRotationPolicyAction type) {
        this.type = type;
    }

    /**
     * Get the {@link KeyRotationPolicyAction type} of the action.
     *
     * @return The {@link KeyRotationPolicyAction type} of the action.
     */
    public KeyRotationPolicyAction getType() {
        return this.type;
    }

    /**
     * Get the time after creation to attempt to rotate. It only applies to the 'rotate' action. It will be in ISO 8601
     * duration format. For example, 90 days is represented as follows: "P90D".
     *
     * @return The time after creation to attempt to rotate.
     */
    public String getTimeAfterCreate() {
        return this.timeAfterCreate;
    }

    /**
     * Get the time after creation to attempt to rotate. It only applies to the 'rotate' action. It will be in ISO 8601
     * duration format. For example, 90 days is represented as follows: "P90D".
     *
     * @param timeAfterCreate The time after creation to attempt to rotate.
     *
     * @return The updated {@link KeyRotationLifetimeAction} object.
     */
    public KeyRotationLifetimeAction setTimeAfterCreate(String timeAfterCreate) {
        this.timeAfterCreate = timeAfterCreate;

        return this;
    }

    /**
     * Get the time before expiry to attempt to rotate or notify. It will be in ISO 8601 duration format. For example,
     * 90 days is represented as follows: "P90D".
     *
     * @return The time before expiry to attempt to rotate or notify.
     */
    public String getTimeBeforeExpiry() {
        return this.timeBeforeExpiry;
    }

    /**
     * Set the time before expiry to attempt to rotate or notify. It will be in ISO 8601 duration format. For example,
     * 90 days is represented as follows: "P90D".
     *
     * @param timeBeforeExpiry The time before expiry to attempt to rotate or notify.
     *
     * @return The updated {@link KeyRotationLifetimeAction} object.
     */
    public KeyRotationLifetimeAction setTimeBeforeExpiry(String timeBeforeExpiry) {
        this.timeBeforeExpiry = timeBeforeExpiry;

        return this;
    }
}
