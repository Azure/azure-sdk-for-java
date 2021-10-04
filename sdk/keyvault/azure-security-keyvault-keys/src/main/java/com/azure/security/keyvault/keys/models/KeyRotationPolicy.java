// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.core.annotation.Immutable;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * The complete key rotation policy that belongs to a key.
 */
@Immutable
public final class KeyRotationPolicy extends KeyRotationPolicyProperties {
    private final String id;
    private final OffsetDateTime createdOn;
    private final OffsetDateTime updatedOn;

    /**
     * Creates an instance of {@link KeyRotationPolicy}.
     *
     * @param id The identifier of the {@link KeyRotationPolicy policy}.
     * @param createdOn The {@link KeyRotationPolicy policy's} created time in UTC.
     * @param updatedOn The {@link KeyRotationPolicy policy's} last updated time in UTC.
     */
    public KeyRotationPolicy(String id, OffsetDateTime createdOn, OffsetDateTime updatedOn) {
        this.id = id;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

    /**
     * Get the identifier of the {@link KeyRotationPolicy policy}.
     *
     * <p>May be undefined if a {@link KeyRotationPolicy policy} has not been explicitly set.</p>
     *
     * @return The identifier of the {@link KeyRotationPolicy policy}.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the {@link KeyRotationPolicy policy's} created time in UTC.
     *
     * <p>May be undefined if a {@link KeyRotationPolicy policy} has not been explicitly set.</p>
     *
     * @return The {@link KeyRotationPolicy policy's} created time in UTC.
     */
    public OffsetDateTime getCreatedOn() {
        return this.createdOn;
    }

    /**
     * Get the {@link KeyRotationPolicy policy's} last updated time in UTC.
     *
     * <p>May be undefined if a {@link KeyRotationPolicy policy} has not been explicitly set.</p>
     *
     * @return The {@link KeyRotationPolicy policy's} last updated time in UTC.
     */
    public OffsetDateTime getUpdatedOn() {
        return this.updatedOn;
    }

    /**
     * Set the optional key expiration period used to define the duration after which a newly rotated key will expire.
     * It should be defined as an ISO 8601 duration. For example, 90 days would be formatted as follows: "P90D", 3
     * months would be "P3M", 48 hours would be "PT48H" and 1 year and 10 days would be "P1Y10D".
     *
     * @param expiryTime The expiry time to set in ISO 8601 format.
     *
     * @return The updated {@link KeyRotationPolicy} object.
     */
    public KeyRotationPolicy setExpiryTime(String expiryTime) {
        this.expiryTime = expiryTime;

        return this;
    }

    /**
     * Set the actions that will be performed by Key Vault over the lifetime of a key.
     *
     * <p>You may also pass an empty array to restore to its default values.</p>
     *
     * @param keyRotationLifetimeActions The {@link KeyRotationLifetimeAction actions} to set.
     *
     * @return The updated {@link KeyRotationPolicy} object.
     */
    public KeyRotationPolicy setLifetimeActions(List<KeyRotationLifetimeAction> keyRotationLifetimeActions) {
        this.keyRotationLifetimeActions = keyRotationLifetimeActions;

        return this;
    }
}
