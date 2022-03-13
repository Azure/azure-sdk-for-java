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

    /**
     * Gets the URI of the Azure KeyVault.
     *
     * @return the URI of the Azure KeyVault
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the URI of the Azure KeyVault.
     *
     * @param uri the URI of the Azure KeyVault
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Gets the tenant ID.
     *
     * @return the tenant ID
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * Sets the tenant ID.
     *
     * @param tenantId the tenant ID
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * Gets the client ID.
     *
     * @return the client ID
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Sets the client ID.
     *
     * @param clientId the client ID
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Gets the client secret.
     *
     * @return the client secret
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Sets the client secret.
     *
     * @param clientSecret the client secret
     */
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    /**
     * Gets the managed identity.
     *
     * @return the managed identity
     */
    public String getManagedIdentity() {
        return managedIdentity;
    }

    /**
     * Sets the managed identity.
     *
     * @param managedIdentity the managed identity
     */
    public void setManagedIdentity(String managedIdentity) {
        this.managedIdentity = managedIdentity;
    }

    /**
     * Gets the JCA properties.
     *
     * @return the JCA properties
     */
    public JcaProperties getJca() {
        return jca;
    }

    /**
     * Sets the JCA properties.
     *
     * @param jca the JCA properties
     */
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

        /**
         * Gets the refresh certificates when there is an untrusted certificate.
         *
         * @return the refresh certificates when there is an untrusted certificate
         */
        public String getRefreshCertificatesWhenHaveUnTrustCertificate() {
            return refreshCertificatesWhenHaveUnTrustCertificate;
        }

        /**
         * Sets the refresh certificates when there is an untrusted certificate.
         *
         * @param refreshCertificatesWhenHaveUnTrustCertificate the refresh certificates when there is an untrusted
         * certificate
         */
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

        /**
         * Gets the certificates refresh interval.
         *
         * @return the certificates refresh interval
         */
        public long getCertificatesRefreshInterval() {
            return certificatesRefreshInterval;
        }

        /**
         * Sets the certificates refresh interval.
         *
         * @param certificatesRefreshInterval the certificates refresh interval
         */
        public void setCertificatesRefreshInterval(long certificatesRefreshInterval) {
            this.certificatesRefreshInterval = certificatesRefreshInterval;
        }

        /**
         * Gets the override trust manager factory.
         *
         * @return the override trust manager factory.
         */
        public String getOverrideTrustManagerFactory() {
            return overrideTrustManagerFactory;
        }

        /**
         * Sets the override trust manager factory.
         *
         * @param overrideTrustManagerFactory the override trust manager factory
         */
        public void setOverrideTrustManagerFactory(String overrideTrustManagerFactory) {
            this.overrideTrustManagerFactory = overrideTrustManagerFactory;
        }

        /**
         * Gets the disable hostname verification.
         *
         * @return the disable hostname verification
         */
        public String getDisableHostnameVerification() {
            return disableHostnameVerification;
        }

        /**
         * Sets the disable hostname verification.
         *
         * @param disableHostnameVerification the disable hostname verification
         */
        public void setDisableHostnameVerification(String disableHostnameVerification) {
            this.disableHostnameVerification = disableHostnameVerification;
        }
    }
}
