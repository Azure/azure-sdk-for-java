// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.storage.common.AzureStorageProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AzureStorageConfigurationTests {

    private static final String STORAGE_CONNECTION_STRING_PATTERN = "DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net";
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureStorageConfiguration.class));

    @Test
    void configurationPropertiesShouldBind() {
        String accountName = "test-account-name";
        String connectionString = String.format(STORAGE_CONNECTION_STRING_PATTERN, accountName, "test-key");
        String endpoint = String.format("https://%s.storage.core.windows.net", accountName);
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.endpoint=" + endpoint,
                "spring.cloud.azure.storage.account-key=test-key",
                "spring.cloud.azure.storage.sas-token=test-sas-token",
                "spring.cloud.azure.storage.connection-string=" + connectionString,
                "spring.cloud.azure.storage.account-name=test-account-name"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureStorageProperties.class);
                AzureStorageProperties properties = context.getBean(AzureStorageProperties.class);
                assertEquals(endpoint, properties.getEndpoint());
                assertEquals("test-key", properties.getAccountKey());
                assertEquals("test-sas-token", properties.getSasToken());
                assertEquals(connectionString, properties.getConnectionString());
                assertEquals(accountName, properties.getAccountName());
            });
    }

}
