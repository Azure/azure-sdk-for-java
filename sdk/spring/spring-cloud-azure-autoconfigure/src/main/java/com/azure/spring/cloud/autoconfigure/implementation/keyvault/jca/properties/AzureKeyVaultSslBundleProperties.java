// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties;

import org.springframework.boot.autoconfigure.ssl.SslBundleProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;

/**
 * Azure Key Vault SSL Bundle properties.
 *
 * @since 5.21.0
 */
public class AzureKeyVaultSslBundleProperties extends SslBundleProperties {

    /**
     * Azure Key Vault endpoint.
     */
    private String endpoint;

    /**
     * Whether to enable refresh certificate when get untrusted certificate.
     */
    private boolean refreshCertificatesWhenHaveUntrustedCertificate;

    /**
     * Time interval to refresh all Key Vault certificate.
     */
    private Duration certificatesRefreshInterval;

    /**
     * Whether to inherit JCA properties, merge credential and profile properties into current bundle properties.
     */
    private boolean inherit = true;

    @NestedConfigurationProperty
    private final AzureKeyVaultJcaCertificatePathsProperties certificatePaths = new AzureKeyVaultJcaCertificatePathsProperties();

    @NestedConfigurationProperty
    private final AzureKeyVaultJcaTokenCredentialConfigurationProperties credential = new AzureKeyVaultJcaTokenCredentialConfigurationProperties();

    @NestedConfigurationProperty
    private final AzureKeyVaultJcaProfileConfigurationProperties profile = new AzureKeyVaultJcaProfileConfigurationProperties();

    public boolean isInherit() {
        return inherit;
    }

    public void setInherit(boolean inherit) {
        this.inherit = inherit;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
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

    public AzureKeyVaultJcaCertificatePathsProperties getCertificatePaths() {
        return certificatePaths;
    }

    public AzureKeyVaultJcaTokenCredentialConfigurationProperties getCredential() {
        return credential;
    }

    public AzureKeyVaultJcaProfileConfigurationProperties getProfile() {
        return profile;
    }
}
