/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.models;

import com.microsoft.azure.SubResource;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describes a reference to Key Vault Secret.
 */
public class KeyVaultSecretReference {
    /**
     * Gets or sets the URL referencing a secret in a Key Vault.
     */
    @JsonProperty(required = true)
    private String secretUrl;

    /**
     * Gets or sets the Relative URL of the Key Vault containing the secret.
     */
    @JsonProperty(required = true)
    private SubResource sourceVault;

    /**
     * Get the secretUrl value.
     *
     * @return the secretUrl value
     */
    public String getSecretUrl() {
        return this.secretUrl;
    }

    /**
     * Set the secretUrl value.
     *
     * @param secretUrl the secretUrl value to set
     */
    public void setSecretUrl(String secretUrl) {
        this.secretUrl = secretUrl;
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
