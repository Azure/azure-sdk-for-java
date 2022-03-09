// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;

/**
 * Represents the configurable options to import a key.
 */
@Fluent
public class ImportKeyOptions extends KeyProperties {
    /**
     * The JSON Web Key to import.
     */
    private final JsonWebKey key;

    /**
     * The hardware protected indicator for the key.
     */
    private Boolean hardwareProtected;

    /**
     * Creates instance of {@link ImportKeyOptions}.
     *
     * @param name The name of the key.
     * @param key The key material to import.
     */
    public ImportKeyOptions(String name, JsonWebKey key) {
        super.name = name;
        this.key = key;
    }

    /**
     * Set whether the key being imported is of HSM type or not.
     *
     * @param hardwareProtected The HSM value to set.
     *
     * @return The {@link ImportKeyOptions} object itself.
     */
    public ImportKeyOptions setHardwareProtected(Boolean hardwareProtected) {
        this.hardwareProtected = hardwareProtected;

        return this;
    }

    /**
     * Get the HSM value of the key being imported.
     *
     * @return The HSM value.
     */
    public Boolean isHardwareProtected() {
        return this.hardwareProtected;
    }

    /**
     * Set a value that indicates if the key is enabled.
     *
     * @param enabled The enabled value to set.
     *
     * @return The updated {@link ImportKeyOptions} object.
     */
    @Override
    public ImportKeyOptions setEnabled(Boolean enabled) {
        this.enabled = enabled;

        return this;
    }

    /**
     * Get the key material of the key being imported.
     *
     * @return The key material.
     */
    public JsonWebKey getKey() {
        return key;
    }

    /**
     * Set the {@link OffsetDateTime key expiration time} in UTC.
     *
     * @param expiresOn The {@link OffsetDateTime key expiration time} in UTC.
     *
     * @return The updated {@link ImportKeyOptions} object.
     */
    @Override
    public ImportKeyOptions setExpiresOn(OffsetDateTime expiresOn) {
        this.expiresOn = expiresOn;

        return this;
    }

    /**
     * Set the {@link OffsetDateTime key's notBefore time} in UTC.
     *
     * @param notBefore The {@link OffsetDateTime key's notBefore time} in UTC.
     *
     * @return The updated {@link ImportKeyOptions} object.
     */
    @Override
    public ImportKeyOptions setNotBefore(OffsetDateTime notBefore) {
        this.notBefore = notBefore;

        return this;
    }
}
