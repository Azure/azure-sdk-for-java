// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;

/**
 * The {@link KeyVaultSetting} model.
 */
@Immutable
public final class KeyVaultSetting {
    private final String name;
    private final Object value;
    private final KeyVaultSettingType type;
    private final ClientLogger logger = new ClientLogger(KeyVaultSetting.class);

    /**
     * Creates a new {@link KeyVaultSetting setting} with the specified details.
     *
     * @param name The name of the {@link KeyVaultSetting setting}.
     * @param value The value of the {@link KeyVaultSetting setting}.
     * @param type The data type of the contents of the {@link KeyVaultSetting setting}.
     */
    public KeyVaultSetting(String name, String value, KeyVaultSettingType type) {
        this.name = name;
        this.type = type;

        if (type == KeyVaultSettingType.BOOLEAN) {
            this.value = Boolean.valueOf(value);
        } else {
            this.value = value;
        }
    }

    /**
     * Creates a new {@link KeyVaultSetting setting} with the specified details.
     *
     * @param name The name of the {@link KeyVaultSetting setting}.
     * @param value The value of the {@link KeyVaultSetting setting}.
     */
    public KeyVaultSetting(String name, Boolean value) {
        this.name = name;
        this.value = value;
        this.type = KeyVaultSettingType.BOOLEAN;
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
     * Get the value of the {@link KeyVaultSetting} as a {@link Boolean}.
     *
     * @return The value of the {@link KeyVaultSetting} as a {@link Boolean}.
     */
    public Boolean asBoolean() {
        if (type != KeyVaultSettingType.BOOLEAN) {
            throw logger.logExceptionAsError(
                new UnsupportedOperationException(String.format("Cannot get setting value as %s from setting value of "
                    + "type %s", KeyVaultSettingType.BOOLEAN, this.getType())));
        }

        return this.value == null ? null : (Boolean) this.value;
    }

    /**
     * Get the value of the {@link KeyVaultSetting} as a {@link String}.
     *
     * @return The value of the {@link KeyVaultSetting} as a {@link String}.
     */
    public String asString() {
        return this.value == null ? null : this.value.toString();
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
