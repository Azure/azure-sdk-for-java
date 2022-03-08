// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.blob;

import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.core.resource.AzureStorageBlobProtocolResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for a {@link AzureStorageBlobProtocolResolver}.
 */
@ConditionalOnClass({ AzureStorageBlobProtocolResolver.class })
@ConditionalOnProperty(value = "spring.cloud.azure.storage.blob.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.storage.blob", name = { "account-name", "endpoint", "connection-string" })
public class AzureStorageBlobResourceAutoConfiguration {

    /**
     * Autoconfigure the {@link AzureStorageBlobProtocolResolver} instance.
     * @return the storage blob protocol resolver.
     */
    @Bean
    @ConditionalOnMissingBean
    public AzureStorageBlobProtocolResolver azureStorageBlobProtocolResolver() {
        return new AzureStorageBlobProtocolResolver();
    }

}
