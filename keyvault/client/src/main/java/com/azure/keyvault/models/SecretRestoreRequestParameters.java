// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault.models;

import com.azure.common.implementation.Base64Url;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SecretRestoreRequestParameters {
    /**
     * The backup blob associated with the secret.
     */
    @JsonProperty(value = "value", required = true)
    private Base64Url secretBackup;

    /**
     * Get the secretBackup value.
     *
     * @return the secretBackup value
     */
    public byte[] secretBackup() {
        if (this.secretBackup == null) {
            return new byte[0];
        }
        return this.secretBackup.decodedBytes();
    }

    /**
     * Set the secretBackup value.
     *
     * @param secretBackup the secretBackup value to set
     * @return the SecretRestoreRequestParameters object itself.
     */
    public SecretRestoreRequestParameters withSecretBackup(byte[] secretBackup) {
        if (secretBackup == null) {
            this.secretBackup = null;
        } else {
            this.secretBackup = Base64Url.encode(secretBackup);
        }
        return this;
    }

}
