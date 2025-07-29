// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties;

import org.springframework.boot.autoconfigure.ssl.SslBundleProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Azure Key Vault SSL Bundle properties.
 *
 * @since 5.21.0
 */
@ConfigurationProperties(AzureKeyVaultSslBundleProperties.PREFIX)
public class AzureKeyVaultSslBundleProperties {

    public static final String PREFIX = "spring.ssl.bundle";

    private final Map<String, KeyVaultSslBundleProperties> keyvault = new HashMap<>();

    public Map<String, KeyVaultSslBundleProperties> getKeyvault() {
        return keyvault;
    }

    public static class KeyVaultSslBundleProperties extends SslBundleProperties {

        /**
         * Whether to use this ssl bundle for client authentication.
         */
        private boolean forClientAuth;
        /**
         * Key Vault keystore properties.
         */
        @NestedConfigurationProperty
        private final KeyStoreProperties keystore = new KeyStoreProperties();

        /**
         * Key Vault truststore properties.
         */
        @NestedConfigurationProperty
        private final KeyStoreProperties truststore = new KeyStoreProperties();

        public boolean isForClientAuth() {
            return forClientAuth;
        }

        public void setForClientAuth(boolean forClientAuth) {
            this.forClientAuth = forClientAuth;
        }

        public KeyStoreProperties getKeystore() {
            return keystore;
        }

        public KeyStoreProperties getTruststore() {
            return truststore;
        }
    }

    public static class KeyStoreProperties {

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
        private final CertificatePathsProperties certificatePaths = new CertificatePathsProperties();

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

        public CertificatePathsProperties getCertificatePaths() {
            return certificatePaths;
        }
    }

    public static class CertificatePathsProperties {

        /**
         * The path to put custom certificates.
         */
        private String custom;

        /**
         * The path to put well-known certificates.
         */
        private String wellKnown;


        public String getCustom() {
            return custom;
        }

        public void setCustom(String custom) {
            this.custom = custom;
        }

        public String getWellKnown() {
            return wellKnown;
        }

        public void setWellKnown(String wellKnown) {
            this.wellKnown = wellKnown;
        }
    }
}
