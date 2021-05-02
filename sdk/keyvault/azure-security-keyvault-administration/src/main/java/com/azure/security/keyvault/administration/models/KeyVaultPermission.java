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
    private final List<String> allowedActions;
    private final List<String> notActions;
    private final List<KeyVaultDataAction> allowedDataActions;
    private final List<KeyVaultDataAction> notDataActions;

    /**
     * Creates a new {@link KeyVaultPermission} with the specified allowed and forbidden actions and data actions.
     *
     * @param allowedActions The actions this {@link KeyVaultPermission permission} allows.
     * @param notActions The actions this {@link KeyVaultPermission permission} forbids.
     * @param allowedDataActions The data actions this {@link KeyVaultPermission permission} allows.
     * @param notDataActions The data actions this {@link KeyVaultPermission permission} forbids.
     */
    public KeyVaultPermission(List<String> allowedActions, List<String> notActions,
                              List<KeyVaultDataAction> allowedDataActions, List<KeyVaultDataAction> notDataActions) {
        this.allowedActions = allowedActions;
        this.notActions = notActions;
        this.allowedDataActions = allowedDataActions;
        this.notDataActions = notDataActions;
    }

    /**
     * Get the actions this {@link KeyVaultPermission permission} allows.
     *
     * @return The allowed actions.
     */
    public List<String> getAllowedActions() {
        return allowedActions;
    }

    /**
     * Get the actions this {@link KeyVaultPermission permission} forbids.
     *
     * @return The forbidden actions.
     */
    public List<String> getNotActions() {
        return notActions;
    }

    /**
     * Get the data actions this {@link KeyVaultPermission permission} allows.
     *
     * @return The allowed data actions.
     */
    public List<KeyVaultDataAction> getAllowedDataActions() {
        return allowedDataActions;
    }

    /**
     * Get the data actions this {@link KeyVaultPermission permission} forbids.
     *
     * @return The forbidden data actions.
     */
    public List<KeyVaultDataAction> getNotDataActions() {
        return notDataActions;
    }
}
