// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.blob;

import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.core.resource.AzureStorageBlobProtocolResolver;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Spring Resource with Azure Storage Blob support.
 *
 *  @since 4.0.0
 */
@ConditionalOnClass({ AzureStorageBlobProtocolResolver.class })
@ConditionalOnProperty(value = "spring.cloud.azure.storage.blob.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefixes = { "spring.cloud.azure.storage.blob", "spring.cloud.azure.storage" }, name = { "account-name", "endpoint", "connection-string" })
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
