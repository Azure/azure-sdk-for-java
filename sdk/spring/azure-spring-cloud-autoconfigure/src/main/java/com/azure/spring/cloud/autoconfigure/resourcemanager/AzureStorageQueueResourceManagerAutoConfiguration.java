// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.resourcemanager;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.storage.queue.AzureStorageQueueProperties;
import com.azure.spring.cloud.resourcemanager.connectionstring.StorageQueueArmConnectionStringProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

/**
 * An auto-configuration for Service Bus
 *
 * @author Warren Zhu
 */
@ConditionalOnProperty(prefix = AzureStorageQueueProperties.PREFIX, value = "enabled", matchIfMissing = true)
@ConditionalOnBean(AzureResourceManager.class)
public class AzureStorageQueueResourceManagerAutoConfiguration extends AzureServiceResourceManagerConfigurationBase {

    private final AzureStorageQueueProperties storageQueueProperties;

    public AzureStorageQueueResourceManagerAutoConfiguration(AzureResourceManager azureResourceManager,
                                                             AzureStorageQueueProperties storageQueueProperties) {
        super(azureResourceManager);
        this.storageQueueProperties = storageQueueProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureStorageQueueProperties.PREFIX, value = "account-name")
    @Order
    public StorageQueueArmConnectionStringProvider storageQueueArmConnectionStringProvider() {
        return new StorageQueueArmConnectionStringProvider(this.azureResourceManager,
                                                           this.storageQueueProperties.getResource(),
                                                           this.storageQueueProperties.getAccountName());
    }

}

