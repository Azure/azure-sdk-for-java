// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault;

import com.azure.spring.cloud.autoconfigure.properties.AbstractAzureHttpConfigurationProperties;
import com.azure.spring.core.properties.retry.HttpRetryProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Common properties for Azure Key Vault
 */
public class AzureKeyVaultProperties extends AbstractAzureHttpConfigurationProperties {

    // TODO (xiada): the default vault url
    private String endpoint;

    @NestedConfigurationProperty
    private final HttpRetryProperties retry = new HttpRetryProperties();

    @Override
    public HttpRetryProperties getRetry() {
        return retry;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
