// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.spring.cloud.autoconfigure.eventhubs.properties.AzureEventHubProperties;
import com.azure.spring.service.storage.blob.BlobServiceClientBuilderFactory;
import com.azure.storage.blob.BlobContainerAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_FACTORY_BEAN_NAME;
import static com.azure.spring.core.properties.AzurePropertiesUtils.mergeAzureCommonProperties;

/**
 * Configures a {@link BlobCheckpointStore}
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ BlobCheckpointStore.class, EventHubClientBuilder.class})
@ConditionalOnProperty(prefix = "spring.cloud.azure.eventhubs.processor.checkpoint-store", name = { "container-name", "account-name" })
public class AzureBlobCheckpointStoreConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureBlobCheckpointStoreConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public BlobCheckpointStore blobCheckpointStore(
        @Qualifier(EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_FACTORY_BEAN_NAME)
            BlobServiceClientBuilderFactory factory,
        AzureEventHubProperties eventHubProperties,
        ObjectProvider<BlobCheckpointStoreContainerInitializer> initializers) {
        final BlobContainerAsyncClient blobContainerAsyncClient = factory
            .build()
            .buildAsyncClient()
            .getBlobContainerAsyncClient(eventHubProperties.getProcessor().getCheckpointStore().getContainerName());

        initializers.ifAvailable(initializer -> initializer.init(blobContainerAsyncClient));

        return new BlobCheckpointStore(blobContainerAsyncClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public BlobCheckpointStoreContainerInitializer blobCheckpointStoreContainerInitializer() {
        return containerAsyncClient -> {
            if (Boolean.FALSE.equals(containerAsyncClient.exists().block(Duration.ofSeconds(3)))) {
                LOGGER.debug("The checkpoint store container [{}] doesn't exist, will create the blob container now.",
                    containerAsyncClient.getBlobContainerName());
                containerAsyncClient.create().block(Duration.ofSeconds(3));
            }
        };
    }

    @Bean(EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_FACTORY_BEAN_NAME)
    @ConditionalOnMissingBean(name = EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_FACTORY_BEAN_NAME)
    public BlobServiceClientBuilderFactory eventHubProcessorBlobServiceClientBuilderFactory(AzureEventHubProperties eventHubProperties) {
        return new BlobServiceClientBuilderFactory(getCheckpointStoreProperties(eventHubProperties));
    }

    private AzureEventHubProperties.Processor.BlobCheckpointStore getCheckpointStoreProperties(
        AzureEventHubProperties ehProperties) {

        AzureEventHubProperties.Processor.BlobCheckpointStore result = new AzureEventHubProperties.Processor
            .BlobCheckpointStore();
        AzureEventHubProperties.Processor.BlobCheckpointStore csProperties = ehProperties.getProcessor()
                                                                                       .getCheckpointStore();

        mergeAzureCommonProperties(ehProperties, csProperties, result);
        BeanUtils.copyProperties(csProperties, result);

        return result;
    }

}
