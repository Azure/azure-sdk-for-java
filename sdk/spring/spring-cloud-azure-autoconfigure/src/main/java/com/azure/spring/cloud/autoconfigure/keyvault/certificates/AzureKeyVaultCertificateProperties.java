// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.certificates;

import com.azure.security.keyvault.certificates.CertificateServiceVersion;
import com.azure.spring.cloud.autoconfigure.keyvault.AzureKeyVaultProperties;
import com.azure.spring.service.keyvault.certificates.KeyVaultCertificateProperties;

/**
 * Properties for Azure Key Vault Certificate.
 */
public class AzureKeyVaultCertificateProperties extends AzureKeyVaultProperties implements KeyVaultCertificateProperties {

    public static final String PREFIX = "spring.cloud.azure.keyvault.certificate";

    private CertificateServiceVersion serviceVersion;

    public CertificateServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(CertificateServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
    }
}
