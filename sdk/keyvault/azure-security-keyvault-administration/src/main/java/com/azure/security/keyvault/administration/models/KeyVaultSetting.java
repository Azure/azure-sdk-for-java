// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.annotation.Immutable;

/**
 * The {@link KeyVaultSetting} model.
 */
@Immutable
public final class KeyVaultSetting {
    private final String name;
    private final String value;
    private final KeyVaultSettingType type;

    /**
     * Creates a new {@link KeyVaultSetting setting} with the specified details.
     *
     * @param name The name of the {@link KeyVaultSetting setting}.
     * @param value The value of the {@link KeyVaultSetting setting}.
     * @param type The data type of the contents of the {@link KeyVaultSetting setting}.
     */
    public KeyVaultSetting(String name, String value, KeyVaultSettingType type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    /**
     * Gets the name of the {@link KeyVaultSetting}.
     *
     * @return The name of the {@link KeyVaultSetting}.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the value of the {@link KeyVaultSetting}.
     *
     * @return The value of the {@link KeyVaultSetting}.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Get the type of the {@link KeyVaultSetting}'s value.
     *
     * @return The type of the {@link KeyVaultSetting}'s value.
     */
    public KeyVaultSettingType getType() {
        return this.type;
    }
}
