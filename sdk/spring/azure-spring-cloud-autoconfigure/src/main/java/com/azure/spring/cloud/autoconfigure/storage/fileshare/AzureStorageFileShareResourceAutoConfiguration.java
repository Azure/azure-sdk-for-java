// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.fileshare;

import com.azure.spring.core.resource.AzureStorageFileProtocolResolver;
import com.azure.storage.file.share.ShareServiceClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for a {@link AzureStorageFileProtocolResolver}.
 */
@ConditionalOnClass({ ShareServiceClient.class, AzureStorageFileProtocolResolver.class })
@AzureStorageFileShareAutoConfiguration.ConditionalOnStorageFileShare
public class AzureStorageFileShareResourceAutoConfiguration  {

    @Bean
    @ConditionalOnMissingBean
    public AzureStorageFileProtocolResolver azureStorageFileProtocolResolver() {
        return new AzureStorageFileProtocolResolver();
    }

}
