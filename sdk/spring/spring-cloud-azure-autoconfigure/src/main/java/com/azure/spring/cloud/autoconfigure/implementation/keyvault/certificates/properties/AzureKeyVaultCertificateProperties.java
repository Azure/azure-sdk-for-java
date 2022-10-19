// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.certificates.properties;

import com.azure.security.keyvault.certificates.CertificateServiceVersion;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.common.AzureKeyVaultProperties;
import com.azure.spring.cloud.service.implementation.keyvault.certificates.CertificateClientProperties;

/**
 * Azure Key Vault Certificate properties.
 *
 * @since 4.0.0
 */
public class AzureKeyVaultCertificateProperties extends AzureKeyVaultProperties implements CertificateClientProperties {

    public static final String PREFIX = "spring.cloud.azure.keyvault.certificate";

    /**
     * The version of Azure Key Vault Certificate Service.
     */
    private CertificateServiceVersion serviceVersion;

    /**
     *
     * @return The {@link CertificateServiceVersion}.
     */
    public CertificateServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    /**
     *
     * @param serviceVersion The {@link CertificateServiceVersion}.
     */
    public void setServiceVersion(CertificateServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
    }
}
