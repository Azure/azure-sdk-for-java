// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.storage.blob;

import com.azure.spring.cloud.core.resource.AzureStorageBlobProtocolResolver;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Spring Resource with Azure Storage Blob support.
 *
 *  @since 4.0.0
 */
@ConditionalOnClass({ AzureStorageBlobProtocolResolver.class })
@Conditional(AzureStorageBlobAutoConfigurationCondition.class)
public class AzureStorageBlobResourceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    AzureStorageBlobProtocolResolver azureStorageBlobProtocolResolver() {
        return new AzureStorageBlobProtocolResolver();
    }

}
