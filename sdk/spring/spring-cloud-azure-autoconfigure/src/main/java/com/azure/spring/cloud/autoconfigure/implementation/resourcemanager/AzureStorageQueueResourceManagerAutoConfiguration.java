// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.resourcemanager;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.implementation.condition.ConditionalOnMissingProperty;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.storage.queue.properties.AzureStorageQueueProperties;
import com.azure.spring.cloud.resourcemanager.implementation.connectionstring.StorageQueueArmConnectionStringProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Storage Queue resource manager support.
 *
 * @since 4.0.0
 */
@ConditionalOnProperty(prefix = AzureStorageQueueProperties.PREFIX, value = "enabled", matchIfMissing = true)
@ConditionalOnBean(AzureResourceManager.class)
@ConditionalOnClass(StorageQueueArmConnectionStringProvider.class)
@AutoConfigureAfter(AzureResourceManagerAutoConfiguration.class)
@EnableConfigurationProperties(StorageQueueResourceMetadata.class)
public class AzureStorageQueueResourceManagerAutoConfiguration extends AzureServiceResourceManagerConfigurationBase {

    private final StorageQueueResourceMetadata resourceMetadata;

    AzureStorageQueueResourceManagerAutoConfiguration(AzureResourceManager azureResourceManager,
                                                      StorageQueueResourceMetadata resourceMetadata) {
        super(azureResourceManager);
        this.resourceMetadata = resourceMetadata;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnMissingProperty({
        AzureStorageQueueProperties.PREFIX + ".connection-string",
        AzureGlobalProperties.PREFIX + ".credential.token-credential-bean-name",
        AzureStorageQueueProperties.PREFIX + ".credential.token-credential-bean-name"
    })
    @Order
    StorageQueueArmConnectionStringProvider storageQueueArmConnectionStringProvider() {
        return new StorageQueueArmConnectionStringProvider(this.azureResourceManager,
                                                           this.resourceMetadata,
                                                           this.resourceMetadata.getName());
    }

}

