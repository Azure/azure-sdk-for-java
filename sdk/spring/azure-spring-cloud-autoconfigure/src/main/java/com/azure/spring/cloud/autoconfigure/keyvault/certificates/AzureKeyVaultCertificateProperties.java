// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.certificates;

import com.azure.security.keyvault.certificates.CertificateServiceVersion;
import com.azure.spring.cloud.autoconfigure.keyvault.AzureKeyVaultProperties;

/**
 * Properties for Azure Key Vault Certificate.
 */
public class AzureKeyVaultCertificateProperties extends AzureKeyVaultProperties {

    public static final String PREFIX = "spring.cloud.azure.keyvault.certificate";

    // TODO (xiada): use enum here?
    private CertificateServiceVersion serviceVersion;

    public CertificateServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(CertificateServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
    }
}
