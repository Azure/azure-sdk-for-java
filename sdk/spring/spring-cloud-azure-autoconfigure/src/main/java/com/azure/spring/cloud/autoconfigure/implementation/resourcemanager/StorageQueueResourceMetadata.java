// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.resourcemanager;

import com.azure.spring.cloud.autoconfigure.implementation.properties.resourcemanager.AzureResourceMetadataConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.storage.queue.properties.AzureStorageQueueProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Azure Storage Queue resource metadata
 */
@ConfigurationProperties(prefix = AzureStorageQueueProperties.PREFIX + ".resource")
public class StorageQueueResourceMetadata extends AzureResourceMetadataConfigurationProperties {

    /**
     * Name of the storage account.
     */
    @Value("${spring.cloud.azure.storage.queue.accountName:}")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
