// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * A result model containing the list of {@link KeyVaultSetting settings} for a given account.
 */
@Immutable
public class KeyVaultListSettingsResult {
    private final List<KeyVaultSetting> value;

    /**
     * Creates an instance of {@link KeyVaultListSettingsResult} class.
     *
     * @param value The list of {@link KeyVaultSetting settings} for the account.
     */
    public KeyVaultListSettingsResult(List<KeyVaultSetting> value) {
        this.value = value;
    }

    /**
     * Get the list of {@link KeyVaultSetting settings} for the account.
     *
     * @return The list of {@link KeyVaultSetting settings} for the account.
     */
    public List<KeyVaultSetting> getValue() {
        return this.value;
    }
}
