package com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties;

public class AzureKeyVaultJcaTokenCredentialConfigurationProperties {

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
