// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.spring.cloud.autoconfigure.context.AzureContextUtils;
import com.azure.spring.cloud.autoconfigure.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.storage.blob.AzureStorageBlobAutoConfiguration;
import com.azure.spring.service.storage.blob.BlobServiceClientBuilderFactory;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AzureBlobCheckpointStoreConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureBlobCheckpointStoreConfiguration.class));


    @Test
    void configureWithoutBlobCheckpointStoreClass() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(BlobCheckpointStore.class))
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.processor.checkpoint-store.container-name=abc",
                "spring.cloud.azure.eventhubs.processor.checkpoint-store.account-name=sa"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureBlobCheckpointStoreConfiguration.class));
    }

    @Test
    void configureWithoutRequiredProperties() {
        this.contextRunner
            .run(context -> assertThat(context).doesNotHaveBean(AzureBlobCheckpointStoreConfiguration.class));
    }

    @Test
    void configureWithStorageInfo() {
        AzureEventHubsProperties azureEventHubsProperties = new AzureEventHubsProperties();
        azureEventHubsProperties.getProcessor().getCheckpointStore().setAccountName("sa");
        azureEventHubsProperties.getProcessor().getCheckpointStore().setContainerName("abc");
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.processor.checkpoint-store.container-name=abc",
                "spring.cloud.azure.eventhubs.processor.checkpoint-store.account-name=sa"
            )
            .withBean(AzureEventHubsProperties.class, () -> azureEventHubsProperties)
            .withBean(BlobCheckpointStore.class, () -> mock(BlobCheckpointStore.class))
            .run(context -> {
                assertThat(context).hasSingleBean(AzureBlobCheckpointStoreConfiguration.class);
                assertThat(context).hasSingleBean(BlobServiceClientBuilderFactory.class);
                assertThat(context).hasBean(AzureContextUtils.EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_FACTORY_BEAN_NAME);
            });
    }

    @Test
    void shouldWorkWithStorageClientConfiguration() {
        AzureEventHubsProperties azureEventHubProperties = new AzureEventHubsProperties();
        azureEventHubProperties.getProcessor().getCheckpointStore().setAccountName("sa");
        azureEventHubProperties.getProcessor().getCheckpointStore().setContainerName("abc");

        this.contextRunner
            .withUserConfiguration(AzureStorageBlobAutoConfiguration.class)
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.processor.checkpoint-store.container-name=abc",
                "spring.cloud.azure.eventhubs.processor.checkpoint-store.account-name=sa",
                "spring.cloud.azure.storage.blob.account-name=sa",
                "spring.cloud.azure.storage.blob.container-name=abc"
            )
            .withBean(AzureEventHubsProperties.class, () -> azureEventHubProperties)
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean(BlobCheckpointStore.class, () -> mock(BlobCheckpointStore.class))
            .run(context -> {
                assertThat(context).hasSingleBean(AzureBlobCheckpointStoreConfiguration.class);
                assertThat(context).hasSingleBean(AzureStorageBlobAutoConfiguration.class);
                assertThat(context).hasBean(AzureContextUtils.EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_FACTORY_BEAN_NAME);
                assertThat(context).hasBean(AzureContextUtils.STORAGE_BLOB_CLIENT_BUILDER_FACTORY_BEAN_NAME);

                assertThat(context).hasSingleBean(BlobServiceClientBuilder.class);
                assertThat(context).hasSingleBean(BlobContainerAsyncClient.class);

                assertThat(context).has(new Condition<>(c -> {
                    String[] beanNamesForType = c.getBeanNamesForType(BlobServiceClientBuilderFactory.class);
                    return beanNamesForType.length == 2;
                }, "There should be two beans of type BlobServiceClientBuilderFactory"));
            });
    }
}
