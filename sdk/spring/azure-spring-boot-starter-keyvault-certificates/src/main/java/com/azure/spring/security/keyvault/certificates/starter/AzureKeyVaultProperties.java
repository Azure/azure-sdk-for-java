// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.security.keyvault.certificates.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * This is used to generate spring-configuration-metadata.json
 *
 * @see <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-configuration-metadata.html">Metadata</a>
 */
@EnableConfigurationProperties({ AzureKeyVaultProperties.class })
@ConfigurationProperties("azure.keyvault")
public class AzureKeyVaultProperties {
    /**
     * The URI to the Azure Key Vault used
     */
    private String uri;
    /**
     * The The Tenant ID for your Azure Key Vault (needed if you are not using managed identity).
     */
    private String tenantId;
    /**
     * The Client ID that has been setup with access to your Azure Key Vault (needed if you are not using managed identity).
     */
    private String clientId;
    /**
     * TThe Client Secret that will be used for accessing your Azure Key Vault (needed if you are not using managed identity).
     */
    private String clientSecret;
    /**
     * The user-assigned managed identity object-id to use.
     */
    private String managedIdentity;
    private JcaProperties jca;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getManagedIdentity() {
        return managedIdentity;
    }

    public void setManagedIdentity(String managedIdentity) {
        this.managedIdentity = managedIdentity;
    }

    public JcaProperties getJca() {
        return jca;
    }

    public void setJca(JcaProperties jca) {
        this.jca = jca;
    }

    /**
     * Jca properties
     */
    public static class JcaProperties {
        /**
         * To configure Spring Cloud Gateway for outbound SSL, set overrideTrustManagerFactory = true.
         */
        private String overrideTrustManagerFactory;
        /**
         * To configure refresh certificate when get untrusted certificate.
         */
        private String refreshCertificatesWhenHaveUnTrustCertificate;

        public String getRefreshCertificatesWhenHaveUnTrustCertificate() {
            return refreshCertificatesWhenHaveUnTrustCertificate;
        }

        public void setRefreshCertificatesWhenHaveUnTrustCertificate(String refreshCertificatesWhenHaveUnTrustCertificate) {
            this.refreshCertificatesWhenHaveUnTrustCertificate = refreshCertificatesWhenHaveUnTrustCertificate;
        }

        /**
         * If you are developing you can completely disable the certificate and hostname validation altogether by
         * setting disableHostnameVerification = true. Note: this is NOT recommended for production!
         */
        private String disableHostnameVerification;
        /**
         * To enable auto refresh certificate, set certificatesRefreshInterval as refresh interval. The unit of time is milliseconds.
         */
        private long certificatesRefreshInterval;

        public long getCertificatesRefreshInterval() {
            return certificatesRefreshInterval;
        }

        public void setCertificatesRefreshInterval(long certificatesRefreshInterval) {
            this.certificatesRefreshInterval = certificatesRefreshInterval;
        }

        public String getOverrideTrustManagerFactory() {
            return overrideTrustManagerFactory;
        }

        public void setOverrideTrustManagerFactory(String overrideTrustManagerFactory) {
            this.overrideTrustManagerFactory = overrideTrustManagerFactory;
        }

        public String getDisableHostnameVerification() {
            return disableHostnameVerification;
        }

        public void setDisableHostnameVerification(String disableHostnameVerification) {
            this.disableHostnameVerification = disableHostnameVerification;
        }
    }
}
