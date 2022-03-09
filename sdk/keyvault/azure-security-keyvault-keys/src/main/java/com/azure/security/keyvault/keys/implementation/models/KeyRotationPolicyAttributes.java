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
     * Get the optional key expiration period used to define the duration after which a newly rotated key will expire.
     * It should be at least 28 days and should be defined as an ISO 8601 duration. For example, 90 days would be
     * "P90D", 3 months would be "P3M" and 1 year and 10 days would be "P1Y10D". See
     * <a href="https://wikipedia.org/wiki/ISO_8601#Durations">Wikipedia</a> for more information on ISO 8601 durations.
     *
     * @return The expiry time in ISO 8601 duration format.
     */
    public String getExpiryTime() {
        return this.expiryTime;
    }

    /**
     * Set the optional key expiration period used to define the duration after which a newly rotated key will expire.
     * It should be at least 28 days and should be defined as an ISO 8601 duration. For example, 90 days would be
     * "P90D", 3 months would be "P3M" and 1 year and 10 days would be "P1Y10D". See
     * <a href="https://wikipedia.org/wiki/ISO_8601#Durations">Wikipedia</a> for more information on ISO 8601 durations.
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
     * Get the created time in UTC.
     *
     * @return The created time in UTC.
     */
    public Long getCreatedOn() {
        return this.createdOn;
    }

    /**
     * Get the last updated time in UTC.
     *
     * @return The the last updated time in UTC.
     */
    public Long getUpdatedOn() {
        return this.updatedOn;
    }
}
