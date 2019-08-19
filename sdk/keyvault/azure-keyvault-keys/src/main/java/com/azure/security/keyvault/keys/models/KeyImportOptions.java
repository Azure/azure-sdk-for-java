// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.security.keyvault.keys.models.webkey.JsonWebKey;

public class KeyImportOptions extends KeyBase {

    /**
     * The Key Material.
     */
    private final JsonWebKey keyMaterial;

    /**
     * The hsm indicator for the key.
     */
    private Boolean hsm;

    /**
     * Creates instance of KeyImportOptions.
     * @param name The name of the key.
     * @param keyMaterial The key material to import.
     */
    public KeyImportOptions(String name, JsonWebKey keyMaterial) {
        super.name = name;
        this.keyMaterial = keyMaterial;
    }

    /**
     * Set whether the key being imported is of hsm type or not.
     * @param hsm The hsm value to set.
     * @return the KeyImportOptions object itself.
     */
    public KeyImportOptions hsm(Boolean hsm) {
        this.hsm = hsm;
        return this;
    }

    /**
     * Get the hsm value of the key being imported.
     * @return the hsm value.
     */
    public Boolean hsm() {
        return this.hsm;
    }

    /**
     * Get the key material of the key being imported.
     * @return the key material.
     */
    public JsonWebKey keyMaterial() {
        return keyMaterial;
    }

}
