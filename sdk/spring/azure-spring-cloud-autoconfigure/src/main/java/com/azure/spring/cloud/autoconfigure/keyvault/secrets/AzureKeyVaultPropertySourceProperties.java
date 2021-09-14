// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.secrets;

import com.azure.security.keyvault.secrets.SecretServiceVersion;
import com.azure.spring.cloud.autoconfigure.properties.AzureHttpConfigurationProperties;

import java.time.Duration;
import java.util.List;

import static com.azure.spring.keyvault.KeyVaultPropertySource.DEFAULT_AZURE_KEYVAULT_PROPERTYSOURCE_NAME;

/**
 * Configurations to set when Azure Key Vault is used as an external property source.
 */
public class AzureKeyVaultPropertySourceProperties extends AzureHttpConfigurationProperties {

    public static final Duration DEFAULT_REFRESH_INTERVAL = Duration.ofMinutes(30);

    private String vaultUrl;
    private SecretServiceVersion serviceVersion;

    private String name = DEFAULT_AZURE_KEYVAULT_PROPERTYSOURCE_NAME;
    /**
     * Defines the constant for the property that enables/disables case-sensitive keys.
     */
    private Boolean caseSensitive;
    private List<String> secretKeys;
    private Duration refreshInterval = DEFAULT_REFRESH_INTERVAL;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVaultUrl() {
        return vaultUrl;
    }

    public void setVaultUrl(String vaultUrl) {
        this.vaultUrl = vaultUrl;
    }

    public SecretServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(SecretServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public Boolean getCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(Boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public List<String> getSecretKeys() {
        return secretKeys;
    }

    public void setSecretKeys(List<String> secretKeys) {
        this.secretKeys = secretKeys;
    }

    public Duration getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(Duration refreshInterval) {
        this.refreshInterval = refreshInterval;
    }
}
