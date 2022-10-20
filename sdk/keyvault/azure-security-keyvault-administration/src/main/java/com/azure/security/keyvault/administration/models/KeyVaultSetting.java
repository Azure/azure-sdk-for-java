// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.annotation.Immutable;

/**
 * The {@link KeyVaultSetting} model.
 */
@Immutable
public final class KeyVaultSetting {
    private String name;
    private String value;
    private KeyVaultSettingType type;

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

    KeyVaultSetting setName(String name) {
        this.name = name;

        return this;
    }

    /**
     * Get the value of the {@link KeyVaultSetting}.
     *
     * @return The value of the {@link KeyVaultSetting}.
     */
    public String getValue() {
        return this.value;
    }

    KeyVaultSetting setValue(String value) {
        this.value = value;

        return this;
    }

    /**
     * Get the type of the {@link KeyVaultSetting}'s value.
     *
     * @return The type of the {@link KeyVaultSetting}'s value.
     */
    public KeyVaultSettingType getType() {
        return this.type;
    }

    KeyVaultSetting setType(KeyVaultSettingType type) {
        this.type = type;

        return this;
    }
}
