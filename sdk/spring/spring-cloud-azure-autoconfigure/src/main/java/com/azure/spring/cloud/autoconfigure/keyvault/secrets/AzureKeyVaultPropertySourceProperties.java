// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.secrets;

import com.azure.security.keyvault.secrets.SecretServiceVersion;
import com.azure.spring.cloud.autoconfigure.properties.core.AbstractAzureHttpConfigurationProperties;

import java.time.Duration;
import java.util.List;

import static com.azure.spring.cloud.autoconfigure.keyvault.env.KeyVaultPropertySource.DEFAULT_AZURE_KEYVAULT_PROPERTYSOURCE_NAME;

/**
 * Configurations to set when Azure Key Vault is used as an external property source.
 *
 * @since 4.0.0
 */
public class AzureKeyVaultPropertySourceProperties extends AbstractAzureHttpConfigurationProperties {

    public static final Duration DEFAULT_REFRESH_INTERVAL = Duration.ofMinutes(30);

    private String endpoint;
    private SecretServiceVersion serviceVersion;

    private String name = DEFAULT_AZURE_KEYVAULT_PROPERTYSOURCE_NAME;
    /**
     * Defines the constant for the property that enables/disables case-sensitive keys.
     */
    private Boolean caseSensitive;
    private List<String> secretKeys;
    private Duration refreshInterval = DEFAULT_REFRESH_INTERVAL;

    /**
     *
     * @return The name of this property source.
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name The name of this property source.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return Endpoint of Azure Key Vault secrets used for this property source.
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     *
     * @param endpoint Endpoint of Azure Key Vault secrets used for this property source.
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     *
     * @return Service version of Azure Key Vault secrets used for this property source.
     */
    public SecretServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    /**
     *
     * @param serviceVersion Service version of Azure Key Vault secrets used for this property source.
     */
    public void setServiceVersion(SecretServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    /**
     *
     * @return Whether the secret key is case-sensitive.
     */
    public Boolean getCaseSensitive() {
        return caseSensitive;
    }

    /**
     *
     * @param caseSensitive Whether the secret key is case-sensitive.
     */
    public void setCaseSensitive(Boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    /**
     *
     * @return The secret keys supported for this property source.
     */
    public List<String> getSecretKeys() {
        return secretKeys;
    }

    /**
     *
     * @param secretKeys The secret keys supported for this property source.
     */
    public void setSecretKeys(List<String> secretKeys) {
        this.secretKeys = secretKeys;
    }

    /**
     *
     * @return Time interval to refresh all Key Vault secrets.
     */
    public Duration getRefreshInterval() {
        return refreshInterval;
    }

    /**
     *
     * @param refreshInterval Time interval to refresh all Key Vault secrets.
     */
    public void setRefreshInterval(Duration refreshInterval) {
        this.refreshInterval = refreshInterval;
    }
}
