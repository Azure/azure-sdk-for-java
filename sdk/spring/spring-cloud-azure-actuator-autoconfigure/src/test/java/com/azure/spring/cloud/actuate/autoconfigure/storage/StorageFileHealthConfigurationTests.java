// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.autoconfigure.storage;

import com.azure.spring.cloud.actuate.storage.StorageFileHealthIndicator;
import com.azure.storage.file.share.ShareServiceAsyncClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class StorageFileHealthConfigurationTests {

    private static final String MOCK_URL = "https://example.org/bigly_fake_url";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withPropertyValues("spring.cloud.azure.storage.fileshare.account-name=test")
        .withConfiguration(AutoConfigurations.of(StorageFileHealthConfiguration.class))
        .withUserConfiguration(TestFileConfigurationConnection.class);

    @Test
    void runShouldCreateIndicator() {
        this.contextRunner.run((context) -> assertThat(context).hasSingleBean(StorageFileHealthIndicator.class));
    }

    @Test
    void runWhenDisabledShouldNotCreateIndicator() {
        this.contextRunner
            .withPropertyValues("management.health.azure-storage.enabled:false")
            .run((context) -> assertThat(context).doesNotHaveBean(StorageFileHealthIndicator.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class TestFileConfigurationConnection {

        @Bean
        ShareServiceAsyncClient shareServiceAsyncClient() {
            return Mockito.mock(ShareServiceAsyncClient.class);
        }
    }
}
