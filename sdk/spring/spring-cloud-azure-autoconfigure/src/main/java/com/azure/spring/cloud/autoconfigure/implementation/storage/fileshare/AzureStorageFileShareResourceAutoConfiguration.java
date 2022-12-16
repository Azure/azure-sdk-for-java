// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.storage.fileshare;

import com.azure.spring.cloud.autoconfigure.implementation.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.core.resource.AzureStorageFileProtocolResolver;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Spring Resource with Azure Storage File Share support.
 *
 * @since 4.0.0
 */
@ConditionalOnClass({ AzureStorageFileProtocolResolver.class })
@ConditionalOnProperty(value = { "spring.cloud.azure.storage.fileshare.enabled", "spring.cloud.azure.storage.enabled" }, havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(
    prefixes = { "spring.cloud.azure.storage.fileshare", "spring.cloud.azure.storage" },
    name = { "account-name", "endpoint", "connection-string" })
public class AzureStorageFileShareResourceAutoConfiguration  {

    @Bean
    @ConditionalOnMissingBean
    AzureStorageFileProtocolResolver azureStorageFileProtocolResolver() {
        return new AzureStorageFileProtocolResolver();
    }

}
