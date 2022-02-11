// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.security.keyvault.keys.implementation.models.KeyRotationPolicyAttributes;
import com.azure.security.keyvault.keys.implementation.models.LifetimeAction;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * The complete key rotation policy that belongs to a key.
 */
public final class KeyRotationPolicy {
    @JsonProperty(value = "id", access = JsonProperty.Access.WRITE_ONLY)
    private String id;

    private List<KeyRotationLifetimeAction> lifetimeActions;
    private KeyRotationPolicyAttributes attributes;
    private OffsetDateTime createdOn;
    private OffsetDateTime updatedOn;

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
     * Get the actions that will be performed by Key Vault over the lifetime of a key.
     *
     * <p>You may also pass an empty array to restore to its default values.</p>
     *
     * @return The {@link KeyRotationLifetimeAction actions} in this {@link KeyRotationPolicy policy}.
     */
    public List<KeyRotationLifetimeAction> getLifetimeActions() {
        return this.lifetimeActions;
    }

    /**
     * Set the actions that will be performed by Key Vault over the lifetime of a key.
     *
     * <p>You may also pass an empty array to restore to its default values.</p>
     *
     * @param lifetimeActions The {@link KeyRotationLifetimeAction actions} to set.
     *
     * @return The updated {@link KeyRotationPolicy} object.
     */
    public KeyRotationPolicy setLifetimeActions(List<KeyRotationLifetimeAction> lifetimeActions) {
        this.lifetimeActions = lifetimeActions;

        return this;
    }

    /**
     * Get the optional key expiration period used to define the duration after which a newly rotated key will expire.
     * It should be at least 28 days and should be defined as an ISO 8601 duration. For example, 90 days would be
     * "P90D", 3 months would be "P3M" and 1 year and 10 days would be "P1Y10D". See
     * <a href="https://wikipedia.org/wiki/ISO_8601#Durations">Wikipedia</a> for more information on ISO 8601 durations.
     *
     * @return The expiration time in ISO 8601 format.
     */
    public String getExpiresIn() {
        return this.attributes == null ? null : this.attributes.getExpiryTime();
    }

    /**
     * Set the optional key expiration period used to define the duration after which a newly rotated key will expire.
     * It should be at least 28 days and should be defined as an ISO 8601 duration. For example, 90 days would be
     * "P90D", 3 months would be "P3M" and 1 year and 10 days would be "P1Y10D". See
     * <a href="https://wikipedia.org/wiki/ISO_8601#Durations">Wikipedia</a> for more information on ISO 8601 durations.
     *
     * @param expiresIn The expiration time to set in ISO 8601 duration format.
     *
     * @return The updated {@link KeyRotationPolicy} object.
     */
    public KeyRotationPolicy setExpiresIn(String expiresIn) {
        if (attributes == null) {
            this.attributes = new KeyRotationPolicyAttributes();
        }

        this.attributes.setExpiryTime(expiresIn);

        return this;
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

    @JsonProperty(value = "lifetimeActions")
    private void unpackLifetimeActions(List<LifetimeAction> lifetimeActions) {
        if (lifetimeActions != null) {
            this.lifetimeActions = new ArrayList<>();

            for (LifetimeAction lifetimeAction : lifetimeActions) {
                this.lifetimeActions.add(new KeyRotationLifetimeAction(lifetimeAction.getAction().getType())
                    .setTimeBeforeExpiry(lifetimeAction.getTrigger().getTimeBeforeExpiry())
                    .setTimeAfterCreate(lifetimeAction.getTrigger().getTimeAfterCreate()));
            }
        }
    }

    @JsonProperty("attributes")
    private void unpackAttributes(KeyRotationPolicyAttributes attributes) {
        if (attributes != null) {
            this.attributes = attributes;

            this.createdOn = OffsetDateTime.of(LocalDateTime.ofEpochSecond(attributes.getCreatedOn(), 0, ZoneOffset.UTC),
                ZoneOffset.UTC);
            this.updatedOn = OffsetDateTime.of(LocalDateTime.ofEpochSecond(attributes.getUpdatedOn(), 0, ZoneOffset.UTC),
                ZoneOffset.UTC);
        }
    }
}
