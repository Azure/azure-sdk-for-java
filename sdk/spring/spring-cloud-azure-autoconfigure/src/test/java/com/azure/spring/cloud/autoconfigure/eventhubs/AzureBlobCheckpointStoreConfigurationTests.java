// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.spring.cloud.autoconfigure.TestBuilderCustomizer;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.autoconfigure.storage.blob.AzureStorageBlobAutoConfiguration;
import com.azure.spring.cloud.service.implementation.storage.blob.BlobServiceClientBuilderFactory;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_FACTORY_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.STORAGE_BLOB_CLIENT_BUILDER_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.STORAGE_BLOB_CLIENT_BUILDER_FACTORY_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AzureBlobCheckpointStoreConfigurationTests {

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
                assertThat(context).hasBean(EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_FACTORY_BEAN_NAME);
            });
    }

    @Test
    void shouldWorkWithStorageClientConfiguration() {
        AzureEventHubsProperties azureEventHubsProperties = new AzureEventHubsProperties();
        azureEventHubsProperties.getProcessor().getCheckpointStore().setAccountName("sa");
        azureEventHubsProperties.getProcessor().getCheckpointStore().setContainerName("abc");

        this.contextRunner
            .withUserConfiguration(AzureStorageBlobAutoConfiguration.class)
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.processor.checkpoint-store.container-name=abc",
                "spring.cloud.azure.eventhubs.processor.checkpoint-store.account-name=sa",
                "spring.cloud.azure.storage.blob.account-name=sa",
                "spring.cloud.azure.storage.blob.container-name=abc"
            )
            .withBean(AzureEventHubsProperties.class, () -> azureEventHubsProperties)
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean(BlobCheckpointStore.class, () -> mock(BlobCheckpointStore.class))
            .run(context -> {
                assertThat(context).hasSingleBean(AzureBlobCheckpointStoreConfiguration.class);
                assertThat(context).hasSingleBean(AzureStorageBlobAutoConfiguration.class);
                assertThat(context).hasBean(EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_FACTORY_BEAN_NAME);
                assertThat(context).hasBean(STORAGE_BLOB_CLIENT_BUILDER_FACTORY_BEAN_NAME);

                assertThat(context).hasBean(EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_BEAN_NAME);
                assertThat(context).hasBean(STORAGE_BLOB_CLIENT_BUILDER_BEAN_NAME);

                assertThat(context).hasSingleBean(BlobContainerAsyncClient.class);

                assertThat(context).has(new Condition<>(c -> {
                    String[] beanNamesForType = c.getBeanNamesForType(BlobServiceClientBuilderFactory.class);
                    return beanNamesForType.length == 2;
                }, "There should be two beans of type BlobServiceClientBuilderFactory"));
            });
    }

    @Test
    void customizerShouldBeCalled() {
        StorageBlobBuilderCustomizer customizer = new StorageBlobBuilderCustomizer();
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.processor.checkpoint-store.container-name=abc",
                "spring.cloud.azure.eventhubs.processor.checkpoint-store.account-name=sa"
            )
            .withBean(AzureEventHubsProperties.class, AzureEventHubsProperties::new)
            .withBean("customizer1", StorageBlobBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", StorageBlobBuilderCustomizer.class, () -> customizer)
            .run(context -> assertThat(customizer.getCustomizedTimes()).isEqualTo(2));
    }

    @Test
    void otherCustomizerShouldNotBeCalled() {
        StorageBlobBuilderCustomizer customizer = new StorageBlobBuilderCustomizer();
        OtherBuilderCustomizer otherBuilderCustomizer = new OtherBuilderCustomizer();
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.processor.checkpoint-store.container-name=abc",
                "spring.cloud.azure.eventhubs.processor.checkpoint-store.account-name=sa"
            )
            .withBean(AzureEventHubsProperties.class, AzureEventHubsProperties::new)
            .withBean("customizer1", StorageBlobBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", StorageBlobBuilderCustomizer.class, () -> customizer)
            .withBean("customizer3", OtherBuilderCustomizer.class, () -> otherBuilderCustomizer)
            .run(context -> {
                assertThat(customizer.getCustomizedTimes()).isEqualTo(2);
                assertThat(otherBuilderCustomizer.getCustomizedTimes()).isEqualTo(0);
            });
    }

    @Test
    void blobContainerInitializerShouldNotConfigureByDefault() {
        this.contextRunner
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.processor.checkpoint-store.container-name=abc",
                "spring.cloud.azure.eventhubs.processor.checkpoint-store.account-name=sa",
                "spring.cloud.azure.eventhubs.profile.cloud-type=Azure"
            )
            .run(context -> {
                assertThat(context).doesNotHaveBean(BlobCheckpointStoreContainerInitializer.class);
            });
    }

    @Test
    void blobContainerInitializerShouldConfigureWhenEnable() {
        BlobServiceClientBuilderFactory mockFactory = mockBlobServiceClientBuilderFactory();

        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.processor.checkpoint-store.container-name=abc",
                "spring.cloud.azure.eventhubs.processor.checkpoint-store.account-name=sa",
                "spring.cloud.azure.eventhubs.processor.checkpoint-store.create-container-if-not-exists=true"
            )
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
            .withBean(EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_FACTORY_BEAN_NAME,
                BlobServiceClientBuilderFactory.class, () -> mockFactory)
            .run(context -> {
                assertThat(context).hasSingleBean(BlobCheckpointStoreContainerInitializer.class);
            });
    }

    @Test
    void blobContainerInitializerShouldWorkWhenEnableAndWithOtherIntializer() {
        BlobServiceClientBuilderFactory mockFactory = mockBlobServiceClientBuilderFactory();
        TestBlobCheckpointStoreContainerInitializer initializer = new TestBlobCheckpointStoreContainerInitializer();
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.processor.checkpoint-store.container-name=abc",
                "spring.cloud.azure.eventhubs.processor.checkpoint-store.account-name=sa",
                "spring.cloud.azure.eventhubs.processor.checkpoint-store.create-container-if-not-exists=true"
            )
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
            .withBean(EVENT_HUB_PROCESSOR_CHECKPOINT_STORE_STORAGE_CLIENT_BUILDER_FACTORY_BEAN_NAME,
                BlobServiceClientBuilderFactory.class, () -> mockFactory)
            .withBean(BlobCheckpointStoreContainerInitializer.class, () -> initializer)
            .run(context -> {
                Map<String, BlobCheckpointStoreContainerInitializer> beans =
                    context.getBeansOfType(BlobCheckpointStoreContainerInitializer.class);
                assertThat(beans.size()).isEqualTo(2);
                assertThat(initializer.getCalledTimes()).isEqualTo(1);
            });
    }

    private BlobServiceClientBuilderFactory mockBlobServiceClientBuilderFactory() {
        BlobServiceClientBuilderFactory mockFactory = mock(BlobServiceClientBuilderFactory.class);
        BlobServiceClientBuilder mockBuilder = mock(BlobServiceClientBuilder.class);
        BlobServiceAsyncClient mockClient = mock(BlobServiceAsyncClient.class);
        BlobContainerAsyncClient mockContainer = mock(BlobContainerAsyncClient.class);

        when(mockFactory.build()).thenReturn(mockBuilder);
        when(mockBuilder.buildAsyncClient()).thenReturn(mockClient);
        when(mockClient.getBlobContainerAsyncClient(anyString())).thenReturn(mockContainer);
        when(mockContainer.exists()).thenReturn(Mono.just(true));
        return mockFactory;
    }

    private static class TestBlobCheckpointStoreContainerInitializer implements BlobCheckpointStoreContainerInitializer {

        private int calledTimes = 0;
        @Override
        public void init(BlobContainerAsyncClient containerAsyncClient) {
            calledTimes++;
        }

        public int getCalledTimes() {
            return calledTimes;
        }
    }

    private static class StorageBlobBuilderCustomizer extends TestBuilderCustomizer<BlobServiceClientBuilder> {

    }

    private static class OtherBuilderCustomizer extends TestBuilderCustomizer<EventHubClientBuilder> {

    }
}
