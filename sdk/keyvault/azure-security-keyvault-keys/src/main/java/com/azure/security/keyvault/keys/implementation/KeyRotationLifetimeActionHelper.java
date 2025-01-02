// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.implementation;

import com.azure.security.keyvault.keys.implementation.models.LifetimeActionsTrigger;
import com.azure.security.keyvault.keys.implementation.models.LifetimeActionsType;
import com.azure.security.keyvault.keys.models.KeyRotationLifetimeAction;
import com.azure.security.keyvault.keys.models.KeyRotationPolicyAction;

public final class KeyRotationLifetimeActionHelper {
    private static KeyRotationLifetimeActionAccessor accessor;

    public interface KeyRotationLifetimeActionAccessor {
        KeyRotationLifetimeAction createLifetimeAction(LifetimeActionsTrigger trigger, LifetimeActionsType actionsType);

        LifetimeActionsTrigger getTrigger(KeyRotationLifetimeAction lifetimeAction);

        LifetimeActionsType getActionType(KeyRotationLifetimeAction lifetimeAction);
    }

    public static KeyRotationLifetimeAction createLifetimeAction(LifetimeActionsTrigger trigger,
        LifetimeActionsType actionsType) {
        if (accessor == null) {
            new KeyRotationLifetimeAction(KeyRotationPolicyAction.NOTIFY);
        }

        assert accessor != null;
        return accessor.createLifetimeAction(trigger, actionsType);
    }

    public static LifetimeActionsTrigger getTrigger(KeyRotationLifetimeAction lifetimeAction) {
        return accessor.getTrigger(lifetimeAction);
    }

    public static LifetimeActionsType getActionType(KeyRotationLifetimeAction lifetimeAction) {
        return accessor.getActionType(lifetimeAction);
    }

    public static void setAccessor(KeyRotationLifetimeActionAccessor accessor) {
        KeyRotationLifetimeActionHelper.accessor = accessor;
    }

    private KeyRotationLifetimeActionHelper() {
    }
}
