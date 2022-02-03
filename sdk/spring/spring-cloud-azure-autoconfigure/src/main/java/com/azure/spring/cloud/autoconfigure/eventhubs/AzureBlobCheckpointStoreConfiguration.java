// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.spring.cloud.autoconfigure.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.core.factory.Helpers;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Optional;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_BEAN_NAME;

/**
 * Configures a {@link BlobCheckpointStore}
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ BlobCheckpointStore.class, EventHubClientBuilder.class})
@ConditionalOnProperty(prefix = "spring.cloud.azure.eventhubs.processor.checkpoint-store", name = { "container-name", "account-name" })
public class AzureBlobCheckpointStoreConfiguration {

    @Autowired
    private ConfigurationBuilder sdkConfigurationBuilder;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureBlobCheckpointStoreConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public BlobCheckpointStore blobCheckpointStore(
        @Qualifier(EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_BEAN_NAME) BlobContainerClientBuilder builder,
        AzureEventHubsProperties eventHubsProperties,
        ObjectProvider<BlobCheckpointStoreContainerInitializer> initializers) {
        final BlobContainerAsyncClient blobContainerAsyncClient = builder.buildAsyncClient();

        initializers.orderedStream().forEach(i -> i.init(blobContainerAsyncClient));

        return new BlobCheckpointStore(blobContainerAsyncClient);
    }

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
    BlobContainerClientBuilder eventHubProcessorBlobServiceClientBuilder(BlobContainerClientBuilder builder,
                                                                       @Qualifier(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME) TokenCredential defaultTokenCredential,
                                                                       Optional<AzureServiceClientBuilderCustomizer<BlobContainerClientBuilder>> builderCustomizer) {
        return Helpers.configureBuilder(
            new BlobContainerClientBuilder(),
            sdkConfigurationBuilder.section("eventhubs.processor.checkpoint-store").build(),
            defaultTokenCredential,
            builderCustomizer);
    }
}
