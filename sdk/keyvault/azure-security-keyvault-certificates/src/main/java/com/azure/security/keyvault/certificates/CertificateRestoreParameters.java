// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.core.util.Base64Url;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The certificate restore parameters.
 */
class CertificateRestoreParameters {
    /**
     * The backup blob associated with a certificate bundle.
     */
    @JsonProperty(value = "value", required = true)
    private Base64Url certificateBundleBackup;

    /**
     * Get the certificateBundleBackup value.
     *
     * @return the certificateBundleBackup value
     */
    public byte[] certificateBundleBackup() {
        if (this.certificateBundleBackup == null) {
            return new byte[0];
        }
        return this.certificateBundleBackup.decodedBytes();
    }

    /**
     * Set the certificateBundleBackup value.
     *
     * @param certificateBundleBackup the certificateBundleBackup value to set
     * @return the CertificateRestoreParameters object itself.
     */
    CertificateRestoreParameters certificateBundleBackup(byte[] certificateBundleBackup) {
        if (certificateBundleBackup == null) {
            this.certificateBundleBackup = null;
        } else {
            this.certificateBundleBackup = Base64Url.encode(certificateBundleBackup);
        }
        return this;
    }

}
