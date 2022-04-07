// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.service.implementation.storage.blob.BlobServiceClientBuilderFactory;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
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

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_FACTORY_BEAN_NAME;
import static com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils.mergeAzureCommonProperties;

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
    BlobCheckpointStore blobCheckpointStore(
        @Qualifier(EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_BEAN_NAME) BlobServiceClientBuilder builder,
        AzureEventHubsProperties eventHubsProperties,
        ObjectProvider<BlobCheckpointStoreContainerInitializer> initializers) {
        final BlobContainerAsyncClient blobContainerAsyncClient = builder
            .buildAsyncClient()
            .getBlobContainerAsyncClient(eventHubsProperties.getProcessor().getCheckpointStore().getContainerName());

        initializers.orderedStream().forEach(i -> i.init(blobContainerAsyncClient));

        return new BlobCheckpointStore(blobContainerAsyncClient);
    }

    /**
     * The default {@link BlobCheckpointStoreContainerInitializer} to create the storage blob if not exists.
     * @return the default {@link BlobCheckpointStoreContainerInitializer}.
     */
    @Bean
    @ConditionalOnProperty(value = "spring.cloud.azure.eventhubs.processor.checkpoint-store.create-container-if-not-exists", havingValue = "true")
    public BlobCheckpointStoreContainerInitializer blobCheckpointStoreContainerCreationInitializer() {
        return containerAsyncClient -> {
            if (Boolean.FALSE.equals(containerAsyncClient.exists().block(Duration.ofSeconds(3)))) {
                LOGGER.debug("The checkpoint store container [{}] doesn't exist, will create the blob container now.",
                    containerAsyncClient.getBlobContainerName());
                containerAsyncClient.create().block(Duration.ofSeconds(3));
            }
        };
    }

    @Bean(EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_BEAN_NAME)
    @ConditionalOnMissingBean(name = EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_BEAN_NAME)
    BlobServiceClientBuilder eventHubProcessorBlobServiceClientBuilder(
        @Qualifier(EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_FACTORY_BEAN_NAME)
            BlobServiceClientBuilderFactory factory) {
        return factory.build();
    }

    @Bean(EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_FACTORY_BEAN_NAME)
    @ConditionalOnMissingBean(name = EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_FACTORY_BEAN_NAME)
    BlobServiceClientBuilderFactory eventHubProcessorBlobServiceClientBuilderFactory(
        AzureEventHubsProperties eventHubsProperties,
        ObjectProvider<AzureServiceClientBuilderCustomizer<BlobServiceClientBuilder>> customizers) {
        BlobServiceClientBuilderFactory factory =
            new BlobServiceClientBuilderFactory(getCheckpointStoreProperties(eventHubsProperties));

        factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_EVENT_HUBS);
        customizers.orderedStream().forEach(factory::addBuilderCustomizer);
        return factory;
    }

    private AzureEventHubsProperties.Processor.BlobCheckpointStore getCheckpointStoreProperties(
        AzureEventHubsProperties ehProperties) {

        AzureEventHubsProperties.Processor.BlobCheckpointStore result = new AzureEventHubsProperties.Processor
            .BlobCheckpointStore();
        AzureEventHubsProperties.Processor.BlobCheckpointStore csProperties = ehProperties.getProcessor()
                                                                                          .getCheckpointStore();

        mergeAzureCommonProperties(ehProperties, csProperties, result);
        BeanUtils.copyProperties(csProperties, result);

        return result;
    }

}
