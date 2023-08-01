// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * A class describing allowed and forbidden actions and data actions of a {@link KeyVaultRoleDefinition}.
 */
@Immutable
public final class KeyVaultPermission {
    private final List<String> actions;
    private final List<String> notActions;
    private final List<KeyVaultDataAction> dataActions;
    private final List<KeyVaultDataAction> notDataActions;

    /**
     * Creates a new {@link KeyVaultPermission} with the specified allowed and forbidden actions and data actions.
     *
     * @param actions Action permissions that are granted.
     * @param notActions Action permissions that are excluded but not denied. They may be granted by other role
     * definitions assigned to a principal.
     * @param dataActions Data action permissions that are granted.
     * @param notDataActions Data action permissions that are excluded but not denied. They may be granted by other role
     * definitions assigned to a principal.
     */
    public KeyVaultPermission(List<String> actions, List<String> notActions, List<KeyVaultDataAction> dataActions,
                              List<KeyVaultDataAction> notDataActions) {
        this.actions = actions;
        this.notActions = notActions;
        this.dataActions = dataActions;
        this.notDataActions = notDataActions;
    }

    /**
     * Get the action permissions that are granted.
     *
     * @return The action permissions that are granted.
     */
    public List<String> getActions() {
        return actions;
    }

    /**
     * Get the action permissions that are excluded but not denied. They may be granted by other role definitions
     * assigned to a principal
     *
     * @return The action permissions that are excluded but not denied.
     */
    public List<String> getNotActions() {
        return notActions;
    }

    /**
     * Get the data action permissions that are granted.
     *
     * @return The data action permissions that are granted.
     */
    public List<KeyVaultDataAction> getDataActions() {
        return dataActions;
    }

    /**
     * Get the data action permissions that are excluded but not denied.
     *
     * @return The data action permissions that are excluded but not denied.
     */
    public List<KeyVaultDataAction> getNotDataActions() {
        return notDataActions;
    }
}
