// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.autoconfigure.implementation.storage;

import com.azure.spring.cloud.actuator.implementation.storage.StorageQueueHealthIndicator;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.storage.queue.AzureStorageQueueAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class StorageQueueHealthConfigurationTests {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withPropertyValues("spring.cloud.azure.storage.queue.account-name=test")
        .withBean(AzureGlobalProperties.class)
        .withConfiguration(AutoConfigurations.of(AzureStorageQueueAutoConfiguration.class, StorageQueueHealthConfiguration.class));

    @Test
    void runShouldCreateIndicator() {
        this.contextRunner.run((context) -> assertThat(context).hasSingleBean(StorageQueueHealthIndicator.class));
    }

    @Test
    void runWhenDisabledShouldNotCreateIndicator() {
        this.contextRunner.withPropertyValues("management.health.azure-storage-queue.enabled:false")
            .run((context) -> assertThat(context).doesNotHaveBean(StorageQueueHealthIndicator.class));
    }
}
