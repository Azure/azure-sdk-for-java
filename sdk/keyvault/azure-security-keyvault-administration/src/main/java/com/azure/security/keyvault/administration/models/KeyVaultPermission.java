// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * A class describing allowed and denied actions and data actions of a {@link KeyVaultRoleDefinition}.
 */
@Immutable
public final class KeyVaultPermission {
    private final List<String> allowedActions;
    private final List<String> deniedActions;
    private final List<String> allowedDataActions;
    private final List<String> deniedDataActions;

    /**
     * Creates a new {@link KeyVaultPermission} with the specified allowed and denied actions and data actions.
     *
     * @param allowedActions The actions this {@link KeyVaultPermission permission} allows.
     * @param deniedActions The actions this {@link KeyVaultPermission permission} denies.
     * @param allowedDataActions The data actions this {@link KeyVaultPermission permission} allows.
     * @param deniedDataActions The data actions this {@link KeyVaultPermission permission} denies.
     */
    public KeyVaultPermission(List<String> allowedActions, List<String> deniedActions, List<String> allowedDataActions, List<String> deniedDataActions) {
        this.allowedActions = allowedActions;
        this.deniedActions = deniedActions;
        this.allowedDataActions = allowedDataActions;
        this.deniedDataActions = deniedDataActions;
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
    public List<String> getAllowedDataActions() {
        return allowedDataActions;
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
