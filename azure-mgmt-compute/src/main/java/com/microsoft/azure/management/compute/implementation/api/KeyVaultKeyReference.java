/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import com.microsoft.azure.SubResource;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describes a reference to Key Vault Key.
 */
public class KeyVaultKeyReference {
    /**
     * Gets or sets the URL referencing a key in a Key Vault.
     */
    @JsonProperty(required = true)
    private String keyUrl;

    /**
     * Gets or sets the Relative URL of the Key Vault containing the key.
     */
    @JsonProperty(required = true)
    private SubResource sourceVault;

    /**
     * Get the keyUrl value.
     *
     * @return the keyUrl value
     */
    public String keyUrl() {
        return this.keyUrl;
    }

    /**
     * Set the keyUrl value.
     *
     * @param keyUrl the keyUrl value to set
     * @return the KeyVaultKeyReference object itself.
     */
    public KeyVaultKeyReference withKeyUrl(String keyUrl) {
        this.keyUrl = keyUrl;
        return this;
    }

    /**
     * Get the sourceVault value.
     *
     * @return the sourceVault value
     */
    public SubResource sourceVault() {
        return this.sourceVault;
    }

    /**
     * Set the sourceVault value.
     *
     * @param sourceVault the sourceVault value to set
     * @return the KeyVaultKeyReference object itself.
     */
    public KeyVaultKeyReference withSourceVault(SubResource sourceVault) {
        this.sourceVault = sourceVault;
        return this;
    }

}
