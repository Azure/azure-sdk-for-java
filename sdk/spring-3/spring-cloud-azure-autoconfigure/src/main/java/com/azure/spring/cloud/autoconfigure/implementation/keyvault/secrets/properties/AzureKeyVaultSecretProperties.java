// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties;

import com.azure.security.keyvault.secrets.SecretServiceVersion;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.common.AzureKeyVaultProperties;
import com.azure.spring.cloud.service.implementation.keyvault.secrets.SecretClientProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Azure Key Vault Secrets properties.
 *
 * @since 4.0.0
 */
public class AzureKeyVaultSecretProperties extends AzureKeyVaultProperties implements SecretClientProperties {

    public static final String PREFIX = "spring.cloud.azure.keyvault.secret";

    /**
     * Secret service version used when making API requests.
     */
    private SecretServiceVersion serviceVersion;

    /**
     * Azure Key Vault property sources.
     */
    private final List<AzureKeyVaultPropertySourceProperties> propertySources = new ArrayList<>();
    /**
     * Whether to enable the Key Vault property source.
     */
    private boolean propertySourceEnabled = true;

    public SecretServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(SecretServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public List<AzureKeyVaultPropertySourceProperties> getPropertySources() {
        return propertySources;
    }

    public boolean isPropertySourceEnabled() {
        return propertySourceEnabled;
    }

    public void setPropertySourceEnabled(boolean propertySourceEnabled) {
        this.propertySourceEnabled = propertySourceEnabled;
    }
}
