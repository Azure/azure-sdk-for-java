// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhub;

import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.spring.cloud.autoconfigure.storage.blob.BlobServiceClientBuilderFactory;
import com.azure.storage.blob.BlobContainerAsyncClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures a {@link BlobCheckpointStore}
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(BlobCheckpointStore.class)
public class AzureBlobCheckpointStoreConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureEventHubProperties.PREFIX, name = "processor.checkpoint-store.container-name")
    public BlobCheckpointStore blobCheckpointStore(AzureEventHubProperties eventHubProperties) {
        final AzureEventHubProperties.Processor.BlobCheckpointStore checkpointStoreProperties = eventHubProperties
            .getProcessor()
            .getCheckpointStore();

        final BlobContainerAsyncClient blobContainerAsyncClient = new BlobServiceClientBuilderFactory(checkpointStoreProperties)
            .build()
            .buildAsyncClient()
            .getBlobContainerAsyncClient(checkpointStoreProperties.getContainerName());

        return new BlobCheckpointStore(blobContainerAsyncClient);
    }

}
