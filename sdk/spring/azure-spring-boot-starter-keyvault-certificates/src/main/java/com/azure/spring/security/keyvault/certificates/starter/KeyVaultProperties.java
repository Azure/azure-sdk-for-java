package com.azure.spring.security.keyvault.certificates.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


@EnableConfigurationProperties({ KeyVaultProperties.class })
@ConfigurationProperties("azure.keyvault")
public class KeyVaultProperties {
    private String tenantId;
    private String clientId;
    private String clientSecret;
    private String managedIdentity;
    private JcaProperties jca;

    private String uri;

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

    public static class JcaProperties {
        private String overrideTrustManagerFactory;
        private String disableHostnameVerification;

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
