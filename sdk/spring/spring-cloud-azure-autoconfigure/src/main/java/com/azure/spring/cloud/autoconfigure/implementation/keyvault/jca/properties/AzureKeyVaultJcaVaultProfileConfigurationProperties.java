// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties;

/**
 * Azure Key Vault JCA vault profile configuration properties.
 *
 * @since 5.21.0
 */
public class AzureKeyVaultJcaVaultProfileConfigurationProperties {

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
