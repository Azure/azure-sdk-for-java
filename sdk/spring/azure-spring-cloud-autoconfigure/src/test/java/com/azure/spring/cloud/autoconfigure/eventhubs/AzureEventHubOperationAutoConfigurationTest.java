// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.spring.eventhubs.core.EventHubClientFactory;
import com.azure.spring.eventhubs.core.EventHubOperation;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.azure.spring.cloud.autoconfigure.eventhubs.EventHubsTestUtils.CONNECTION_STRING;
import static org.assertj.core.api.Assertions.assertThat;

class AzureEventHubOperationAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubOperationAutoConfiguration.class));

    @Test
    void disableEventHubsShouldNotConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.enabled=false",
                "spring.cloud.azure.eventhubs.namespace=test-namespace"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubOperationAutoConfiguration.class));
    }

    @Test
    void withoutEventHubOperationShouldNotConfigure() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(EventHubOperation.class))
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.namespace=test-namespace"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubOperationAutoConfiguration.class));
    }

    @Test
    void withoutEventHubConnectionShouldNotConfigure() {
        this.contextRunner
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubOperationAutoConfiguration.class));
    }

    @Test
    void connectionInfoProvidedShouldConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING, "test-namespace")
            )
            .withUserConfiguration(AzureEventHubPropertiesTestConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(EventHubOperation.class);
                assertThat(context).hasSingleBean(EventHubClientFactory.class);
                assertThat(context).hasSingleBean(AzureEventHubSharedCredentialClientConfiguration.EventHubServiceClientConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureEventHubSharedCredentialClientConfiguration.EventProcessorServiceClientConfiguration.class);
            });
    }

    @Test
    void connectionInfoAndCheckpointStoreProvidedShouldConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING, "test-namespace")
            )
            .withUserConfiguration(AzureEventHubPropertiesTestConfiguration.class)
            .withBean(CheckpointStore.class, TestCheckpointStore::new)
            .run(context -> {
                assertThat(context).hasSingleBean(EventHubOperation.class);
                assertThat(context).hasSingleBean(EventHubClientFactory.class);
                assertThat(context).hasSingleBean(AzureEventHubSharedCredentialClientConfiguration.EventHubServiceClientConfiguration.class);
                assertThat(context).hasSingleBean(AzureEventHubSharedCredentialClientConfiguration.EventProcessorServiceClientConfiguration.class);
            });
    }

}
