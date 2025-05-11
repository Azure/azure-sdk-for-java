// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.keys.implementation;

import com.azure.v2.security.keyvault.keys.implementation.models.LifetimeActionsTrigger;
import com.azure.v2.security.keyvault.keys.implementation.models.LifetimeActionsType;
import com.azure.v2.security.keyvault.keys.models.KeyRotationLifetimeAction;
import com.azure.v2.security.keyvault.keys.models.KeyRotationPolicyAction;

/**
 * Helper class to create instances of {@link KeyRotationLifetimeAction} and to access its properties. This class is
 * used to allow the implementation to be hidden from the public API.
 */
public final class KeyRotationLifetimeActionHelper {
    private static KeyRotationLifetimeActionAccessor accessor;

    /**
     * Interface to create instances of {@link KeyRotationLifetimeAction} and to access its properties.
     */
    public interface KeyRotationLifetimeActionAccessor {
        /**
         * Creates an instance of {@link KeyRotationLifetimeAction} with the specified trigger and action type.
         *
         * @param trigger The trigger for the lifetime action.
         * @param actionsType The action type for the lifetime action.
         * @return An instance of {@link KeyRotationLifetimeAction}.
         */
        KeyRotationLifetimeAction createLifetimeAction(LifetimeActionsTrigger trigger, LifetimeActionsType actionsType);

        /**
         * Gets the trigger of the specified {@link KeyRotationLifetimeAction}.
         *
         * @param lifetimeAction The lifetime action to get the trigger from.
         * @return The trigger of the specified {@link KeyRotationLifetimeAction}.
         */
        LifetimeActionsTrigger getTrigger(KeyRotationLifetimeAction lifetimeAction);

        /**
         * Gets the action type of the specified {@link KeyRotationLifetimeAction}.
         *
         * @param lifetimeAction The lifetime action to get the action type from.
         * @return The action type of the specified {@link KeyRotationLifetimeAction}.
         */
        LifetimeActionsType getActionType(KeyRotationLifetimeAction lifetimeAction);
    }

    /**
     * Creates an instance of {@link KeyRotationLifetimeAction} with the specified trigger and action type.
     *
     * @param trigger The trigger for the lifetime action.
     * @param actionsType The action type for the lifetime action.
     * @return An instance of {@link KeyRotationLifetimeAction}.
     */
    public static KeyRotationLifetimeAction createLifetimeAction(LifetimeActionsTrigger trigger,
        LifetimeActionsType actionsType) {

        if (accessor == null) {
            new KeyRotationLifetimeAction(KeyRotationPolicyAction.NOTIFY);
        }

        assert accessor != null;
        return accessor.createLifetimeAction(trigger, actionsType);
    }

    /**
     * Gets the trigger of the specified {@link KeyRotationLifetimeAction}.
     *
     * @param lifetimeAction The lifetime action to get the trigger from.
     * @return The trigger of the specified {@link KeyRotationLifetimeAction}.
     */
    public static LifetimeActionsTrigger getTrigger(KeyRotationLifetimeAction lifetimeAction) {
        return accessor.getTrigger(lifetimeAction);
    }

    /**
     * Gets the action type of the specified {@link KeyRotationLifetimeAction}.
     *
     * @param lifetimeAction The lifetime action to get the action type from.
     * @return The action type of the specified {@link KeyRotationLifetimeAction}.
     */
    public static LifetimeActionsType getActionType(KeyRotationLifetimeAction lifetimeAction) {
        return accessor.getActionType(lifetimeAction);
    }

    /**
     * Sets the accessor for the {@link KeyRotationLifetimeAction} class.
     *
     * @param accessor The accessor to set.
     */
    public static void setAccessor(KeyRotationLifetimeActionAccessor accessor) {
        KeyRotationLifetimeActionHelper.accessor = accessor;
    }

    private KeyRotationLifetimeActionHelper() {
        // Private constructor to prevent instantiation.
    }
}
