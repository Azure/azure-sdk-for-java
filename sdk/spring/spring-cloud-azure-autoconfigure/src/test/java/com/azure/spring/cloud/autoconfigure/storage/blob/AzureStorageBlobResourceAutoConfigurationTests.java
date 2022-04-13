// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.blob;

import com.azure.spring.cloud.core.resource.AzureStorageBlobProtocolResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class AzureStorageBlobResourceAutoConfigurationTests {

    private static final String MOCK_URL = "https://test.blob.core.windows.net/";
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureStorageBlobResourceAutoConfiguration.class));

    @Test
    void accountNameShouldConfigure() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.storage.blob.account-name=test")
            .run((context) -> assertThat(context).hasSingleBean(AzureStorageBlobProtocolResolver.class));
    }

    @Test
    void endpointShouldConfigure() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.storage.blob.endpoint=" + MOCK_URL)
            .run((context) -> assertThat(context).hasSingleBean(AzureStorageBlobProtocolResolver.class));
    }

    @Test
    void connectionStringShouldConfigure() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.storage.blob.connection-string=test-connection")
            .run((context) -> assertThat(context).hasSingleBean(AzureStorageBlobProtocolResolver.class));
    }

    @Test
    void runShouldCreateResolver() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.storage.blob.account-name=test")
            .run((context) -> assertThat(context).hasSingleBean(AzureStorageBlobProtocolResolver.class));
    }

    @Test
    void runWhenDisabledShouldNotCreateResolver() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.blob.account-name=test",
                "spring.cloud.azure.storage.blob.enabled:false")
            .run((context) -> assertThat(context).doesNotHaveBean(AzureStorageBlobProtocolResolver.class));
    }
}
