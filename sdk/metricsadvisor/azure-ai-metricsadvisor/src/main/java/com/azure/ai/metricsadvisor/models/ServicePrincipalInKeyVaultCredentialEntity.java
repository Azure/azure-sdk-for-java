// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.ai.metricsadvisor.implementation.util.ServicePrincipalInKeyVaultCredentialEntityAccessor;

/**
 * The service principal stored in a key vault representing the credential entity for a data source.
 */
public final class ServicePrincipalInKeyVaultCredentialEntity extends DataSourceCredentialEntity {
    private String id;
    private String name;
    private String description;
    private String keyVaultEndpoint;
    private String keyVaultClientId;
    private String keyVaultClientSecret;
    private String clientIdSecretName;
    private String clientSecretName;
    private String tenantId;

    static {
        ServicePrincipalInKeyVaultCredentialEntityAccessor.setAccessor(
            new ServicePrincipalInKeyVaultCredentialEntityAccessor.Accessor() {
                @Override
                public void setId(ServicePrincipalInKeyVaultCredentialEntity entity, String id) {
                    entity.setId(id);
                }

                @Override
                public String getKeyVaultClientSecret(ServicePrincipalInKeyVaultCredentialEntity entity) {
                    return entity.getKeyVaultClientSecret();
                }

                @Override
                public String getSecretNameForDataSourceClientId(ServicePrincipalInKeyVaultCredentialEntity entity) {
                    return entity.getSecretNameForDataSourceClientId();
                }

                @Override
                public String getSecretNameForDataSourceClientSecret(
                    ServicePrincipalInKeyVaultCredentialEntity entity) {
                    return entity.getSecretNameForDataSourceClientSecret();
                }
            });
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * Gets the endpoint to the KeyVault storing service principal.
     *
     * @return The KeyVault endpoint.
     */
    public String getKeyVaultEndpoint() {
        return this.keyVaultEndpoint;
    }

    /**
     * Gets the client id to access the KeyVault storing service principal.
     *
     * @return The client id to access the KeyVault.
     */
    public String getKeyVaultClientId() {
        return this.keyVaultClientId;
    }

    /**
     * Gets the tenant id of the service principal.
     *
     * @return The tenant id.
     */
    public String getTenantId() {
        return this.tenantId;
    }

    /**
     * Sets the name.
     *
     * @param name The name
     * @return an updated object with name set
     */
    public ServicePrincipalInKeyVaultCredentialEntity setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the keyVault containing the data source secrets.
     *
     * @param keyVaultEndpoint The keyVault endpoint
     * @param keyVaultClientId The client  id to access the keyVault
     * @param keyVaultClientSecret The client secret to access the keyVault
     * @return an updated object
     */
    public ServicePrincipalInKeyVaultCredentialEntity setKeyVaultForDataSourceSecrets(String keyVaultEndpoint,
                                                                                      String keyVaultClientId,
                                                                                      String keyVaultClientSecret) {
        this.keyVaultEndpoint = keyVaultEndpoint;
        this.keyVaultClientId = keyVaultClientId;
        this.keyVaultClientSecret = keyVaultClientSecret;
        return this;
    }

    /**
     * Sets the name of the keyvault secret holding client id.
     *
     * @param clientIdSecretName The secret name
     * @return an updated object with client id secret name set
     */
    public ServicePrincipalInKeyVaultCredentialEntity setSecretNameForDataSourceClientId(String clientIdSecretName) {
        this.clientIdSecretName = clientIdSecretName;
        return this;
    }

    /**
     * Sets the name of the keyvault secret holding client secret.
     *
     * @param clientSecretName The secret name
     * @return an updated object with client secret name set
     */
    public ServicePrincipalInKeyVaultCredentialEntity setSecretNameForDataSourceClientSecret(String clientSecretName) {
        this.clientSecretName = clientSecretName;
        return this;
    }

    /**
     * Sets the tenant id.
     *
     * @param tenantId The tenant id
     * @return an updated object with client tenant id set
     */
    public ServicePrincipalInKeyVaultCredentialEntity setTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Sets the description.
     *
     * @param description The description
     * @return an updated object with description set
     */
    public ServicePrincipalInKeyVaultCredentialEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    private void setId(String id) {
        this.id = id;
    }

    private String getKeyVaultClientSecret() {
        return this.keyVaultClientSecret;
    }

    private String getSecretNameForDataSourceClientId() {
        return this.clientIdSecretName;
    }

    private String getSecretNameForDataSourceClientSecret() {
        return this.clientSecretName;
    }
}
