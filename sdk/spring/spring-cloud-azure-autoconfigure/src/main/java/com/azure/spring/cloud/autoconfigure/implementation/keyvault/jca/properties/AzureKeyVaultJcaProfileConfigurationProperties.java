package com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties;

public class AzureKeyVaultJcaProfileConfigurationProperties {

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
