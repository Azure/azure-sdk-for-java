// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.autoconfigure.implementation.storage;

import com.azure.spring.cloud.actuator.implementation.storage.StorageFileShareHealthIndicator;
import com.azure.storage.file.share.ShareServiceAsyncClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class StorageFileShareHealthConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withPropertyValues("spring.cloud.azure.storage.fileshare.account-name=test")
        .withConfiguration(AutoConfigurations.of(StorageFileShareHealthConfiguration.class))
        .withUserConfiguration(TestFileConfigurationConnection.class);

    @Test
    void runShouldCreateIndicator() {
        this.contextRunner.run((context) -> assertThat(context).hasSingleBean(StorageFileShareHealthIndicator.class));
    }

    @Test
    void runWhenDisabledShouldNotCreateIndicator() {
        this.contextRunner
            .withPropertyValues("management.health.azure-storage-fileshare.enabled:false")
            .run((context) -> assertThat(context).doesNotHaveBean(StorageFileShareHealthIndicator.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class TestFileConfigurationConnection {

        @Bean
        ShareServiceAsyncClient shareServiceAsyncClient() {
            return Mockito.mock(ShareServiceAsyncClient.class);
        }
    }
}
