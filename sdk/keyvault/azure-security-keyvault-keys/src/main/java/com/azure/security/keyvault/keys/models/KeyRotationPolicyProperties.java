// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * The properties of a key rotation policy that the client can set for a given key.
 *
 * You may also reset the key rotation policy to its default values by setting {@code lifetimeActions} to an empty
 * array.
 *
 * @see KeyRotationPolicy
 */
@Fluent
public class KeyRotationPolicyProperties {
    protected String expiryTime;
    protected List<KeyRotationLifetimeAction> keyRotationLifetimeActions;

    /**
     * Get the optional key expiration period used to define the duration after which a newly rotated key will expire.
     * It should be at least 28 days and defined as an ISO 8601 duration. For example, 90 days would be formatted as
     * follows: "P90D", 3 months would be "P3M", 48 hours would be "PT48H" and 1 year and 10 days would be "P1Y10D", to
     * name a few.
     *
     * @return The expiry time in ISO 8601 format.
     */
    public String getExpiryTime() {
        return this.expiryTime;
    }

    /**
     * Set the optional key expiration period used to define the duration after which a newly rotated key will expire.
     * It should be at least 28 days and defined as an ISO 8601 duration. For example, 90 days would be formatted as
     * follows: "P90D", 3 months would be "P3M", 48 hours would be "PT48H" and 1 year and 10 days would be "P1Y10D", to
     * name a few.
     *
     * @param expiryTime The expiry time to set in ISO 8601 format.
     *
     * @return The updated {@link KeyRotationPolicyProperties} object.
     */
    public KeyRotationPolicyProperties setExpiryTime(String expiryTime) {
        this.expiryTime = expiryTime;

        return this;
    }

    /**
     * Get the actions that will be performed by Key Vault over the lifetime of a key. At the moment,
     * {@link KeyRotationLifetimeAction} can only have two items at maximum: one for rotate, one for notify.
     * The notification time default value is 30 days before expiry and is not configurable.
     *
     * <p>You may also pass an empty array to restore to its default values.</p>
     *
     * @return The {@link KeyRotationLifetimeAction actions} in this {@link KeyRotationPolicyProperties policy}.
     */
    public List<KeyRotationLifetimeAction> getLifetimeActions() {
        return this.keyRotationLifetimeActions;
    }

    /**
     * Set the actions that will be performed by Key Vault over the lifetime of a key. At the moment,
     * {@link KeyRotationLifetimeAction} can only have two items at maximum: one for rotate, one for notify.
     * The notification time default value is 30 days before expiry and is not configurable.
     *
     * <p>You may also pass an empty array to restore to its default values.</p>
     *
     * @param keyRotationLifetimeActions The {@link KeyRotationLifetimeAction actions} to set.
     *
     * @return The updated {@link KeyRotationPolicyProperties} object.
     */
    public KeyRotationPolicyProperties setLifetimeActions(List<KeyRotationLifetimeAction> keyRotationLifetimeActions) {
        this.keyRotationLifetimeActions = keyRotationLifetimeActions;

        return this;
    }
}
