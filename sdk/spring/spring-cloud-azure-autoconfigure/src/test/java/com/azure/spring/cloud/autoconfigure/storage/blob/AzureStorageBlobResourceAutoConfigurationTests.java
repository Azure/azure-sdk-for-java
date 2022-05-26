// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.blob;

import com.azure.spring.cloud.core.resource.AzureStorageBlobProtocolResolver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class AzureStorageBlobResourceAutoConfigurationTests {

    private static final String MOCK_URL = "https://test.blob.core.windows.net/";
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureStorageBlobResourceAutoConfiguration.class));

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.blob.account-name=test-account-name", "spring.cloud.azure.storage.account-name=test-account-name" })
    void accountNameShouldConfigure(String property) {
        this.contextRunner
            .withPropertyValues(property)
            .run((context) -> assertThat(context).hasSingleBean(AzureStorageBlobProtocolResolver.class));
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.blob.endpoint", "spring.cloud.azure.storage.endpoint" })
    void endpointShouldConfigure(String property) {
        this.contextRunner
            .withPropertyValues(property + "=" + MOCK_URL)
            .run((context) -> assertThat(context).hasSingleBean(AzureStorageBlobProtocolResolver.class));
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.blob.connection-string=test-connection", "spring.cloud.azure.storage.connection-string=test-connection" })
    void connectionStringShouldConfigure(String property) {
        this.contextRunner
            .withPropertyValues(property)
            .run((context) -> assertThat(context).hasSingleBean(AzureStorageBlobProtocolResolver.class));
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.blob.account-name=test-account-name", "spring.cloud.azure.storage.account-name=test-account-name" })
    void runShouldCreateResolver(String property) {
        this.contextRunner
            .withPropertyValues(property)
            .run((context) -> assertThat(context).hasSingleBean(AzureStorageBlobProtocolResolver.class));
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.blob.account-name=test-account-name", "spring.cloud.azure.storage.account-name=test-account-name" })
    void runWhenDisabledShouldNotCreateResolver(String property) {
        this.contextRunner
            .withPropertyValues(
                property,
                "spring.cloud.azure.storage.blob.enabled:false")
            .run((context) -> assertThat(context).doesNotHaveBean(AzureStorageBlobProtocolResolver.class));
    }
}
