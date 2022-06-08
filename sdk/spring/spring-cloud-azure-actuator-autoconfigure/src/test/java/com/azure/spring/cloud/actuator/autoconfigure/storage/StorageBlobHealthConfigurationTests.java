// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.autoconfigure.storage;

import com.azure.spring.cloud.actuator.storage.StorageBlobHealthIndicator;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.storage.blob.AzureStorageBlobAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class StorageBlobHealthConfigurationTests {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withPropertyValues("spring.cloud.azure.storage.blob.account-name=test")
        .withBean(AzureGlobalProperties.class)
        .withConfiguration(AutoConfigurations.of(AzureStorageBlobAutoConfiguration.class, StorageBlobHealthConfiguration.class));

    @Test
    void runShouldCreateIndicator() {
        this.contextRunner.run((context) -> assertThat(context).hasSingleBean(StorageBlobHealthIndicator.class));
    }

    @Test
    void runWhenDisabledShouldNotCreateIndicator() {
        this.contextRunner.withPropertyValues("management.health.azure-storage-blob.enabled:false")
            .run((context) -> assertThat(context).doesNotHaveBean(StorageBlobHealthIndicator.class));
    }
}
