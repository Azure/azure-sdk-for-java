/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.models;

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
    public String getKeyUrl() {
        return this.keyUrl;
    }

    /**
     * Set the keyUrl value.
     *
     * @param keyUrl the keyUrl value to set
     */
    public void setKeyUrl(String keyUrl) {
        this.keyUrl = keyUrl;
    }

    /**
     * Get the sourceVault value.
     *
     * @return the sourceVault value
     */
    public SubResource getSourceVault() {
        return this.sourceVault;
    }

    /**
     * Set the sourceVault value.
     *
     * @param sourceVault the sourceVault value to set
     */
    public void setSourceVault(SubResource sourceVault) {
        this.sourceVault = sourceVault;
    }

}
