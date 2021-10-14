// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.queue;

import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.storage.queue.QueueServiceAsyncClient;
import com.azure.storage.queue.QueueServiceClient;
import com.azure.storage.queue.QueueServiceClientBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AzureStorageQueueAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureStorageQueueAutoConfiguration.class));

    @Test
    void configureWithoutQueueServiceClientBuilder() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(QueueServiceClientBuilder.class))
            .withPropertyValues("spring.cloud.azure.storage.queue.account-name=sa")
            .run(context -> assertThat(context).doesNotHaveBean(AzureStorageQueueAutoConfiguration.class));
    }

    @Test
    void configureWithStorageQueueDisabled() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.queue.enabled=false",
                "spring.cloud.azure.storage.queue.account-name=sa"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureStorageQueueAutoConfiguration.class));
    }

    @Test
    void accountNameSetShouldConfigure() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.storage.queue.account-name=sa")
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureStorageQueueAutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureStorageQueueProperties.class);
                assertThat(context).hasSingleBean(QueueServiceClient.class);
                assertThat(context).hasSingleBean(QueueServiceAsyncClient.class);
                assertThat(context).hasSingleBean(QueueServiceClientBuilder.class);
                assertThat(context).hasSingleBean(QueueServiceClientBuilderFactory.class);
            });
    }
}
