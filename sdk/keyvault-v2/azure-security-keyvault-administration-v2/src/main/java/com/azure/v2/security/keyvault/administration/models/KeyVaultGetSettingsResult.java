// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.administration.models;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;

import java.util.List;

/**
 * A result model containing the {@link KeyVaultSetting settings} for a given account.
 */
@Metadata(properties = { MetadataProperties.IMMUTABLE })
public final class KeyVaultGetSettingsResult {
    private final List<KeyVaultSetting> settings;

    /**
     * Creates an instance of {@link KeyVaultGetSettingsResult} class.
     *
     * @param settings The {@link KeyVaultSetting settings} for the account.
     */
    public KeyVaultGetSettingsResult(List<KeyVaultSetting> settings) {
        this.settings = settings;
    }

    /**
     * Get the list of {@link KeyVaultSetting settings} for the account.
     *
     * @return The {@link KeyVaultSetting settings} for the account.
     */
    public List<KeyVaultSetting> getSettings() {
        return this.settings;
    }
}
