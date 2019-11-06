// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.core.util.Base64Url;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The backup key result, containing the backup blob.
 */
class CertificateBackup {
    /**
     * The backup blob containing the backed up key.
     */
    @JsonProperty(value = "value", access = JsonProperty.Access.WRITE_ONLY)
    private Base64Url value;

    /**
     * Get the backup blob.
     *
     * @return the backup blob
     */
    byte[] getValue() {
        if (this.value == null) {
            return new byte[0];
        }
        return this.value.decodedBytes();
    }
}
