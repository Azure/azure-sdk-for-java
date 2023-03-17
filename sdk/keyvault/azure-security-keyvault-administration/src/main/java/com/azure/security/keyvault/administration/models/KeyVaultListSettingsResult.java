// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * A result model containing the list of {@link KeyVaultSetting settings} for a given account.
 */
@Immutable
public final class KeyVaultListSettingsResult {
    private final List<KeyVaultSetting> settings;

    /**
     * Creates an instance of {@link KeyVaultListSettingsResult} class.
     *
     * @param settings The list of {@link KeyVaultSetting settings} for the account.
     */
    public KeyVaultListSettingsResult(List<KeyVaultSetting> settings) {
        this.settings = settings;
    }

    /**
     * Get the list of {@link KeyVaultSetting settings} for the account.
     *
     * @return The list of {@link KeyVaultSetting settings} for the account.
     */
    public List<KeyVaultSetting> getSettings() {
        return this.settings;
    }
}
