// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.core.annotation.Fluent;
import com.azure.security.keyvault.keys.models.webkey.JsonWebKey;

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
     * Get the key material of the key being imported.
     * @return the key material.
     */
    public JsonWebKey getKey() {
        return key;
    }

}
