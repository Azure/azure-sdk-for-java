// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;

/**
 * Azure Key Vault SSL Bundle Key Store properties.
 *
 * @since 5.21.0
 */
public class AzureKeyVaultSslBundleKeyStoreProperties {

    /**
     * The key of Key Vault connection.
     */
    private String keyvaultRef;
    /**
     * Whether to enable refresh certificate when get untrusted certificate.
     */
    private boolean refreshCertificatesWhenHaveUntrustedCertificate;

    /**
     * Time interval to refresh all Key Vault certificate.
     */
    private Duration certificatesRefreshInterval;

    @NestedConfigurationProperty
    private final AzureKeyVaultSslBundleCertificatePathsProperties certificatePaths = new AzureKeyVaultSslBundleCertificatePathsProperties();

    public String getKeyvaultRef() {
        return keyvaultRef;
    }

    public void setKeyvaultRef(String keyvaultRef) {
        this.keyvaultRef = keyvaultRef;
    }

    public boolean isRefreshCertificatesWhenHaveUntrustedCertificate() {
        return refreshCertificatesWhenHaveUntrustedCertificate;
    }

    public void setRefreshCertificatesWhenHaveUntrustedCertificate(boolean refreshCertificatesWhenHaveUntrustedCertificate) {
        this.refreshCertificatesWhenHaveUntrustedCertificate = refreshCertificatesWhenHaveUntrustedCertificate;
    }

    public Duration getCertificatesRefreshInterval() {
        return certificatesRefreshInterval;
    }

    public void setCertificatesRefreshInterval(Duration certificatesRefreshInterval) {
        this.certificatesRefreshInterval = certificatesRefreshInterval;
    }

    public AzureKeyVaultSslBundleCertificatePathsProperties getCertificatePaths() {
        return certificatePaths;
    }
}
