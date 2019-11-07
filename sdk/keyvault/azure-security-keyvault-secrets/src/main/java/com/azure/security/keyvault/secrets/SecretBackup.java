// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.util.Base64Url;
import com.fasterxml.jackson.annotation.JsonProperty;

class SecretBackup {
    /**
     * The backup blob containing the backed up secret.
     */
    @JsonProperty(value = "value", access = JsonProperty.Access.WRITE_ONLY)
    private Base64Url value;

    /**
     * Get the secret backup value as byte array.
     *
     * @return the secret backup value
     */
    public byte[] getValue() {
        if (this.value == null) {
            return new byte[0];
        }
        return this.value.decodedBytes();
    }
}
