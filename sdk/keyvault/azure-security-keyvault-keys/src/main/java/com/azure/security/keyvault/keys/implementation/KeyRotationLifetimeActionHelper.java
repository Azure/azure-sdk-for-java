// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.implementation;

import com.azure.security.keyvault.keys.implementation.models.LifetimeActionsTrigger;
import com.azure.security.keyvault.keys.implementation.models.LifetimeActionsType;
import com.azure.security.keyvault.keys.models.KeyRotationLifetimeAction;

public final class KeyRotationLifetimeActionHelper {
    private static KeyRotationLifetimeActionAccessor accessor;

    public interface KeyRotationLifetimeActionAccessor {
        void setActionType(KeyRotationLifetimeAction action, LifetimeActionsType actionsType);
        LifetimeActionsType getActionType(KeyRotationLifetimeAction action);
        void setTrigger(KeyRotationLifetimeAction action, LifetimeActionsTrigger trigger);
        LifetimeActionsTrigger getTrigger(KeyRotationLifetimeAction action);
    }

    public static void setActionType(KeyRotationLifetimeAction action, LifetimeActionsType actionsType) {
        accessor.setActionType(action, actionsType);
    }

    public static LifetimeActionsType getActionType(KeyRotationLifetimeAction action) {
        return accessor.getActionType(action);
    }

    public static void setTrigger(KeyRotationLifetimeAction action, LifetimeActionsTrigger trigger) {
        accessor.setTrigger(action, trigger);
    }

    public static LifetimeActionsTrigger getTrigger(KeyRotationLifetimeAction action) {
        return accessor.getTrigger(action);
    }

    public static void setAccessor(KeyRotationLifetimeActionAccessor accessor) {
        KeyRotationLifetimeActionHelper.accessor = accessor;
    }

    private KeyRotationLifetimeActionHelper() { }
}
