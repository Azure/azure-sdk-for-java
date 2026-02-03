// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.models;

import com.azure.v2.security.keyvault.keys.implementation.KeyRotationLifetimeActionHelper;
import com.azure.v2.security.keyvault.keys.implementation.KeyRotationPolicyHelper;
import com.azure.v2.security.keyvault.keys.implementation.models.KeyRotationPolicyAttributes;
import com.azure.v2.security.keyvault.keys.implementation.models.LifetimeActions;
import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The complete key rotation policy that belongs to a key.
 */
@Metadata(properties = { MetadataProperties.FLUENT })
public final class KeyRotationPolicy {
    static {
        KeyRotationPolicyHelper.setAccessor(new KeyRotationPolicyHelper.KeyRotationPolicyAccessor() {
            @Override
            public KeyRotationPolicy
                createPolicy(com.azure.v2.security.keyvault.keys.implementation.models.KeyRotationPolicy impl) {

                return new KeyRotationPolicy(impl);
            }

            @Override
            public com.azure.v2.security.keyvault.keys.implementation.models.KeyRotationPolicy
                getImpl(KeyRotationPolicy policy) {

                return policy.impl;
            }
        });
    }

    private final com.azure.v2.security.keyvault.keys.implementation.models.KeyRotationPolicy impl;

    private List<KeyRotationLifetimeAction> lifetimeActions;

    /**
     * Creates an instance of {@link KeyRotationPolicy}.
     */
    public KeyRotationPolicy() {
        this(new com.azure.v2.security.keyvault.keys.implementation.models.KeyRotationPolicy());
    }

    KeyRotationPolicy(com.azure.v2.security.keyvault.keys.implementation.models.KeyRotationPolicy impl) {
        this.impl = impl;
    }

    /**
     * Get the identifier of the {@link KeyRotationPolicy policy}.
     *
     * <p>May be undefined if a {@link KeyRotationPolicy policy} has not been explicitly set.</p>
     *
     * @return The identifier of the {@link KeyRotationPolicy policy}.
     */
    public String getId() {
        return impl.getId();
    }

    /**
     * Get the actions that will be performed by Key Vault over the lifetime of a key.
     *
     * <p>You may also pass an empty array to restore to its default values.</p>
     *
     * @return The {@link KeyRotationLifetimeAction actions} in this {@link KeyRotationPolicy policy}.
     */
    public List<KeyRotationLifetimeAction> getLifetimeActions() {
        if (this.lifetimeActions == null && impl.getLifetimeActions() != null) {
            List<KeyRotationLifetimeAction> mappedActions = new ArrayList<>(impl.getLifetimeActions().size());

            for (LifetimeActions action : impl.getLifetimeActions()) {
                KeyRotationLifetimeAction mappedAction
                    = KeyRotationLifetimeActionHelper.createLifetimeAction(action.getTrigger(), action.getAction());

                mappedActions.add(mappedAction);
            }

            this.lifetimeActions = mappedActions;
        }

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
        if (lifetimeActions == null) {
            this.lifetimeActions = null;
            impl.setLifetimeActions(null);
            return this;
        }

        List<LifetimeActions> mappedActions = new ArrayList<>(lifetimeActions.size());

        for (KeyRotationLifetimeAction action : lifetimeActions) {
            mappedActions.add(new LifetimeActions().setAction(KeyRotationLifetimeActionHelper.getActionType(action))
                .setTrigger(KeyRotationLifetimeActionHelper.getTrigger(action)));
        }

        this.lifetimeActions = lifetimeActions;
        impl.setLifetimeActions(mappedActions);

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
        return impl.getAttributes() == null ? null : impl.getAttributes().getExpiryTime();
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
        if (impl.getAttributes() == null) {
            impl.setAttributes(new KeyRotationPolicyAttributes());
        }

        impl.getAttributes().setExpiryTime(expiresIn);

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
        return impl.getAttributes() == null ? null : impl.getAttributes().getCreated();
    }

    /**
     * Get the {@link KeyRotationPolicy policy's} last updated time in UTC.
     *
     * <p>May be undefined if a {@link KeyRotationPolicy policy} has not been explicitly set.</p>
     *
     * @return The {@link KeyRotationPolicy policy's} last updated time in UTC.
     */
    public OffsetDateTime getUpdatedOn() {
        return impl.getAttributes() == null ? null : impl.getAttributes().getUpdated();
    }
}
