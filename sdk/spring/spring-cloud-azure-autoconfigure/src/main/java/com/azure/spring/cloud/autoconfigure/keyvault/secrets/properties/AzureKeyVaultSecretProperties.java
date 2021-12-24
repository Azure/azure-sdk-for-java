// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.secrets.properties;

import com.azure.security.keyvault.secrets.SecretServiceVersion;
import com.azure.spring.cloud.autoconfigure.keyvault.common.AzureKeyVaultProperties;
import com.azure.spring.cloud.autoconfigure.keyvault.secrets.AzureKeyVaultPropertySourceProperties;
import com.azure.spring.service.keyvault.secrets.SecretClientProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Azure Key Vault Secrets properties.
 *
 * @since 4.0.0
 */
public class AzureKeyVaultSecretProperties extends AzureKeyVaultProperties implements SecretClientProperties {

    public static final String PREFIX = "spring.cloud.azure.keyvault.secret";

    private SecretServiceVersion serviceVersion;

    private final List<AzureKeyVaultPropertySourceProperties> propertySources = new ArrayList<>();
    private Boolean propertySourceEnabled;

    public SecretServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(SecretServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public List<AzureKeyVaultPropertySourceProperties> getPropertySources() {
        return propertySources;
    }

    public Boolean getPropertySourceEnabled() {
        return propertySourceEnabled;
    }

    public void setPropertySourceEnabled(Boolean propertySourceEnabled) {
        this.propertySourceEnabled = propertySourceEnabled;
    }
}
