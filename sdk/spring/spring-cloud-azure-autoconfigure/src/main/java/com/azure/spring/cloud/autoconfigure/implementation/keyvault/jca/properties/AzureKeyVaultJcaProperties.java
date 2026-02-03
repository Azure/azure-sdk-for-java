// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Azure Key Vault JCA vault properties.
 *
 * @since 5.21.0
 */
@ConfigurationProperties(AzureKeyVaultJcaProperties.PREFIX)
public class AzureKeyVaultJcaProperties {

    public static final String PREFIX = "spring.cloud.azure.keyvault.jca";

    private final Map<String, JcaVaultProperties> vaults = new HashMap<>();

    public Map<String, JcaVaultProperties> getVaults() {
        return vaults;
    }

    public static class JcaVaultProperties {
        /**
         * Azure Key Vault endpoint.
         */
        private String endpoint;

        @NestedConfigurationProperty
        private final TokenCredentialProperties credential = new TokenCredentialProperties();

        @NestedConfigurationProperty
        private final ProfileProperties profile = new ProfileProperties();

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public TokenCredentialProperties getCredential() {
            return credential;
        }

        public ProfileProperties getProfile() {
            return profile;
        }
    }

    public static class TokenCredentialProperties {

        /**
         * Client ID to use when performing service principal authentication with Azure Key Vault.
         */
        private String clientId;

        /**
         * Client secret to use when performing service principal authentication with Azure Key Vault.
         */
        private String clientSecret;

        /**
         * Whether to enable managed identity to authenticate with Azure Key Vault. If true and the client-id is set, will
         * use the client ID as user assigned managed identity client ID.
         */
        private boolean managedIdentityEnabled = false;

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

        public boolean isManagedIdentityEnabled() {
            return managedIdentityEnabled;
        }

        public void setManagedIdentityEnabled(boolean managedIdentityEnabled) {
            this.managedIdentityEnabled = managedIdentityEnabled;
        }
    }

    public static class ProfileProperties {

        /**
         * Tenant ID to use when performing service principal authentication with Azure Key Vault.
         */
        private String tenantId;

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }
    }
}
