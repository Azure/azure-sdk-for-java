// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.fileshare;

import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.core.resource.AzureStorageFileProtocolResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for a {@link AzureStorageFileProtocolResolver}.
 */
@ConditionalOnClass({ AzureStorageFileProtocolResolver.class })
@ConditionalOnProperty(value = "spring.cloud.azure.storage.fileshare.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.storage.fileshare", name = { "account-name", "endpoint", "connection-string" })
public class AzureStorageFileShareResourceAutoConfiguration  {

    /**
     * Autoconfigure the {@link AzureStorageFileProtocolResolver} instance.
     * @return the storage file protocol resolver.
     */
    @Bean
    @ConditionalOnMissingBean
    public AzureStorageFileProtocolResolver azureStorageFileProtocolResolver() {
        return new AzureStorageFileProtocolResolver();
    }

}
