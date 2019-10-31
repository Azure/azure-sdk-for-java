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
     * The Key Material.
     */
    private final JsonWebKey key;

    /**
     * The hardware protected indicator for the key.
     */
    private Boolean hardwareProtected;

    /**
     * Creates instance of KeyImportOptions.
     * @param name The name of the key.
     * @param key The key material to import.
     */
    public ImportKeyOptions(String name, JsonWebKey key) {
        super.name = name;
        this.key = key;
    }

    /**
     * Set whether the key being imported is of hsm type or not.
     * @param hardwareProtected The hsm value to set.
     * @return the KeyImportOptions object itself.
     */
    public ImportKeyOptions setHardwareProtected(Boolean hardwareProtected) {
        this.hardwareProtected = hardwareProtected;
        return this;
    }

    /**
     * Get the hsm value of the key being imported.
     * @return the hsm value.
     */
    public Boolean isHardwareProtected() {
        return this.hardwareProtected;
    }

    /**
     * Set the enabled value.
     *
     * @param enabled The enabled value to set
     * @return the updated ImportKeyOptions object itself.
     */
    @Override
    public ImportKeyOptions setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }


    /**
     * Get the key material of the key being imported.
     * @return the key material.
     */
    public JsonWebKey getKey() {
        return key;
    }

    /**
     * Set the {@link OffsetDateTime expires} UTC time.
     *
     * @param expiresOn The expiry time to set for the key.
     * @return the updated ImportKeyOptions object itself.
     */
    @Override
    public ImportKeyOptions setExpiresOn(OffsetDateTime expiresOn) {
        this.expiresOn = expiresOn;
        return this;
    }

    /**
     * Set the {@link OffsetDateTime notBefore} UTC time.
     *
     * @param notBefore The notBefore UTC time to set
     * @return the updated ImportKeyOptions object itself.
     */
    @Override
    public ImportKeyOptions setNotBefore(OffsetDateTime notBefore) {
        this.notBefore = notBefore;
        return this;
    }
}
