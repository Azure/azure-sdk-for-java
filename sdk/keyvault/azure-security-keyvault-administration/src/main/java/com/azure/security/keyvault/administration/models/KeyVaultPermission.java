// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import java.util.List;

/**
 * A class describing allowed and denied actions and data actions of a {@link KeyVaultRoleDefinition}.
 */
public final class KeyVaultPermission {
    private final List<String> actions;
    private final List<String> deniedActions;
    private final List<String> dataActions;
    private final List<String> deniedDataActions;

    /**
     * Creates a new {@link KeyVaultPermission} with the specified allowed and denied actions and data actions.
     *
     * @param actions The actions this {@link KeyVaultPermission permission} allows.
     * @param deniedActions The actions this {@link KeyVaultPermission permission} denies.
     * @param dataActions The data actions this {@link KeyVaultPermission permission} allows.
     * @param deniedDataActions The data actions this {@link KeyVaultPermission permission} denies.
     */
    public KeyVaultPermission(List<String> actions, List<String> deniedActions, List<String> dataActions, List<String> deniedDataActions) {
        this.actions = actions;
        this.deniedActions = deniedActions;
        this.dataActions = dataActions;
        this.deniedDataActions = deniedDataActions;
    }

    /**
     * Get the actions this {@link KeyVaultPermission permission} allows.
     *
     * @return The allowed actions.
     */
    public List<String> getActions() {
        return actions;
    }

    /**
     * Get the actions this {@link KeyVaultPermission permission} denies.
     *
     * @return The denied actions.
     */
    public List<String> getDeniedActions() {
        return deniedActions;
    }

    /**
     * Get the data actions this {@link KeyVaultPermission permission} allows.
     *
     * @return The allowed data actions.
     */
    public List<String> getDataActions() {
        return dataActions;
    }

    /**
     * Get the data actions this {@link KeyVaultPermission permission} denies.
     *
     * @return The denied data actions.
     */
    public List<String> getDeniedDataActions() {
        return deniedDataActions;
    }
}
