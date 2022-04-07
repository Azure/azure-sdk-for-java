// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.keyvault.secret;

import com.azure.security.keyvault.secrets.SecretServiceVersion;
import com.azure.spring.cloud.core.implementation.properties.AzureHttpSdkProperties;
import com.azure.spring.cloud.service.implementation.keyvault.secrets.SecretClientProperties;

/**
 * Properties for Azure Storage File Share service.
 */
class AzureKeyVaultSecretTestProperties extends AzureHttpSdkProperties implements SecretClientProperties {

    private String endpoint;
    private SecretServiceVersion serviceVersion;

    @Override
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public SecretServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(SecretServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
    }
}
