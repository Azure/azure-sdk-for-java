// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A condition to be satisfied for an action to be executed.
 */
@Fluent
public final class LifetimeActionTrigger {
    @JsonProperty(value = "timeAfterCreate")
    private String timeAfterCreate;

    @JsonProperty(value = "timeBeforeExpiry")
    private String timeBeforeExpiry;

    /**
     * Get the time after creation to attempt to rotate in ISO 8601 duration format. For example, 90 days is represented
     * as follows: "P90D".
     *
     * @return The time after creation to attempt to rotate.
     */
    public String getTimeAfterCreate() {
        return this.timeAfterCreate;
    }

    /**
     * Set the time after creation to attempt to rotate in ISO 8601 duration format. For example, 90 days is represented
     * as follows: "P90D".
     *
     * @param timeAfterCreate The time after creation to attempt to rotate.
     *
     * @return The updated {@link LifetimeActionTrigger} object.
     */
    public LifetimeActionTrigger setTimeAfterCreate(String timeAfterCreate) {
        this.timeAfterCreate = timeAfterCreate;

        return this;
    }

    /**
     * Get the time before expiry to attempt to rotate or notify in ISO 8601 duration format. For example, 90 days is
     * represented as follows: "P90D".
     *
     * @return The time before expiry to attempt to rotate or notify.
     */
    public String getTimeBeforeExpiry() {
        return this.timeBeforeExpiry;
    }

    /**
     * Set the time before expiry to attempt to rotate or notify in ISO 8601 duration format. For example, 90 days is
     * represented as follows: "P90D".
     *
     * @param timeBeforeExpiry The time before expiry to attempt to rotate or notify.
     *
     * @return The updated {@link LifetimeActionTrigger} object.
     */
    public LifetimeActionTrigger setTimeBeforeExpiry(String timeBeforeExpiry) {
        this.timeBeforeExpiry = timeBeforeExpiry;

        return this;
    }
}
