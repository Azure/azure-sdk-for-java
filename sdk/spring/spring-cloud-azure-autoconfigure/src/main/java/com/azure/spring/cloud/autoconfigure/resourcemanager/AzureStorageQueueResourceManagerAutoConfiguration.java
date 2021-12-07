// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.resourcemanager;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.storage.queue.properties.AzureStorageQueueProperties;
import com.azure.spring.cloud.resourcemanager.connectionstring.StorageQueueArmConnectionStringProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

/**
 * An auto-configuration for Service Bus
 *
 */
@ConditionalOnProperty(prefix = AzureStorageQueueProperties.PREFIX, value = "enabled", matchIfMissing = true)
@ConditionalOnBean(AzureResourceManager.class)
@AutoConfigureAfter(AzureResourceManagerAutoConfiguration.class)
@EnableConfigurationProperties(StorageQueueResourceMetadata.class)
public class AzureStorageQueueResourceManagerAutoConfiguration extends AzureServiceResourceManagerConfigurationBase {

    private final StorageQueueResourceMetadata resourceMetadata;

    /**
     * Azure Storage Queue Resource Manager Auto Configuration
     * @param azureResourceManager azure Resource Manager
     * @param resourceMetadata resource Metadata
     */
    public AzureStorageQueueResourceManagerAutoConfiguration(AzureResourceManager azureResourceManager,
                                                             StorageQueueResourceMetadata resourceMetadata) {
        super(azureResourceManager);
        this.resourceMetadata = resourceMetadata;
    }

    /**
     * Storage Queue Arm Connection String Provider Bean
     * @return StorageQueueArmConnectionStringProvider Bean
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureStorageQueueProperties.PREFIX, value = "account-name")
    @Order
    public StorageQueueArmConnectionStringProvider storageQueueArmConnectionStringProvider() {
        return new StorageQueueArmConnectionStringProvider(this.azureResourceManager,
                                                           this.resourceMetadata,
                                                           this.resourceMetadata.getName());
    }

}

