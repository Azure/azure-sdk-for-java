// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.fileshare;

import com.azure.spring.cloud.core.resource.AzureStorageFileProtocolResolver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AzureStorageFileShareResourceAutoConfigurationTests {

    private static final String MOCK_URL = "https://test.fileshare.core.windows.net/";
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureStorageFileShareResourceAutoConfiguration.class));

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.fileshare.account-name=test-account-name", "spring.cloud.azure.storage.account-name=test-account-name" })
    void accountNameShouldConfigure(String accoutNameProperty) {
        this.contextRunner
            .withPropertyValues(accoutNameProperty)
            .run(context -> {
                    assertThat(context).hasSingleBean(AzureStorageFileShareResourceAutoConfiguration.class);
                    assertThat(context).hasSingleBean(AzureStorageFileProtocolResolver.class);
                });
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.fileshare.endpoint", "spring.cloud.azure.storage.endpoint" })
    void endpointShouldConfigure(String endpointProperty) {
        this.contextRunner
            .withPropertyValues(endpointProperty + "=" + MOCK_URL)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureStorageFileShareResourceAutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureStorageFileProtocolResolver.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.fileshare.connection-string=test-connection", "spring.cloud.azure.storage.connection-string=test-connection" })
    void connectionStringShouldConfigure(String connectionStringProperty) {
        this.contextRunner
            .withPropertyValues(connectionStringProperty)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureStorageFileShareResourceAutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureStorageFileProtocolResolver.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.fileshare.account-name=test-account-name", "spring.cloud.azure.storage.account-name=test-account-name" })
    void configureWithStorageFileShareResourceDisabled(String accoutNameProperty) {
        this.contextRunner
            .withPropertyValues(
                accoutNameProperty,
                "spring.cloud.azure.storage.fileshare.enabled:false")
            .run((context) -> assertThat(context).doesNotHaveBean(AzureStorageFileShareResourceAutoConfiguration.class));
    }
}
