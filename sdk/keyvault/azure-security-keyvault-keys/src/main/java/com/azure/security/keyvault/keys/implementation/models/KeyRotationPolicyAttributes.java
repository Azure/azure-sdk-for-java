// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The key rotation policy attributes.
 */
@Fluent
public final class KeyRotationPolicyAttributes {
    @JsonProperty(value = "expiryTime")
    private String expiryTime;

    @JsonProperty(value = "created", access = JsonProperty.Access.WRITE_ONLY)
    private Long createdOn;

    @JsonProperty(value = "updated", access = JsonProperty.Access.WRITE_ONLY)
    private Long updatedOn;

    /**
     * Get the {@link KeyRotationPolicy policy's} {@code expiryTime}. It will be applied on the new key version. It
     * should be at least 28 days. It will be in ISO 8601 format. For example, 90 days would be formatted as follows:
     * "P90D", 3 months would be "P3M", 48 hours would be "PT48H" and 1 year and 10 days would be "P1Y10D", to name a
     * few.
     *
     * @return The expiry time in ISO 8601 format.
     */
    public String getExpiryTime() {
        return this.expiryTime;
    }

    /**
     * Set the {@link KeyRotationPolicy policy's} {@code expiryTime}. It will be applied on the new key version. It
     * should be at least 28 days. It will be in ISO 8601 format. For example, 90 days would be formatted as follows:
     * "P90D", 3 months would be "P3M", 48 hours would be "PT48H" and 1 year and 10 days would be "P1Y10D", to name a
     * few.
     *
     * @param expiryTime The expiry time to set in ISO 8601 format.
     *
     * @return The updated {@link KeyRotationPolicyAttributes} object.
     */
    public KeyRotationPolicyAttributes setExpiryTime(String expiryTime) {
        this.expiryTime = expiryTime;

        return this;
    }

    /**
     * Get the {@link KeyRotationPolicy policy's} created time in UTC.
     *
     * @return The {@link KeyRotationPolicy policy's} created time in UTC.
     */
    public Long getCreatedOn() {
        return this.createdOn;
    }

    /**
     * Get the {@link KeyRotationPolicy policy's} last updated time in UTC.
     *
     * @return The the {@link KeyRotationPolicy policy's} last updated time in UTC.
     */
    public Long getUpdatedOn() {
        return this.updatedOn;
    }
}
