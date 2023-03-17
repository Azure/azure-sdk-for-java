// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.CoreUtils;
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
     * Creates a new {@link KeyVaultSetting setting}  with the with the specified details.
     *
     * @param name The name of the {@link KeyVaultSetting setting}.
     * @param value The value of the {@link KeyVaultSetting setting}.
     */
    public KeyVaultSetting(String name, boolean value) {
        if (CoreUtils.isNullOrEmpty(name)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("The 'name' parameter cannot be null or empty"));
        }

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
     * Gets the value of the {@link KeyVaultSetting} as an {@link Object}.
     *
     * @return The value of the {@link KeyVaultSetting} as an {@link Object}.
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * Get the value of the {@link KeyVaultSetting} as a {@code boolean}.
     *
     * @return The value of the {@link KeyVaultSetting} as a {@code boolean}.
     */
    public boolean asBoolean() {
        if (type != KeyVaultSettingType.BOOLEAN) {
            throw logger.logExceptionAsError(
                new UnsupportedOperationException(String.format("Cannot get setting value as %s from setting value of "
                    + "type %s", KeyVaultSettingType.BOOLEAN, this.getType())));
        }

        return (Boolean) this.value;
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
