// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.eventhubs.kafka;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.AzureEventHubsAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsConnectionDetails;
import com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureEventHubsKafkaOAuth2AutoConfiguration;
import com.azure.spring.cloud.core.provider.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
class AzureEventHubsKafkaAutoConfigurationTests {

    private static final String CONNECTION_STRING = "Endpoint=sb://test-namespace.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=key";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(
            AzureEventHubsAutoConfiguration.class,
            AzureEventHubsKafkaAutoConfiguration.class,
            AzureEventHubsKafkaOAuth2AutoConfiguration.class))
        .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new);

    @Test
    void connectionStringRegistersProvider() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + CONNECTION_STRING
            )
            .run(context -> {
                assertThat(context).hasSingleBean(StaticConnectionStringProvider.class);
                StaticConnectionStringProvider<?> provider = context.getBean(StaticConnectionStringProvider.class);
                assertThat(provider.getServiceType()).isEqualTo(AzureServiceType.EVENT_HUBS);
                assertThat(provider.getConnectionString()).isEqualTo(CONNECTION_STRING);
            });
    }

    @Test
    void connectionDetailsRegistersProvider() {
        this.contextRunner
            .withBean(AzureEventHubsConnectionDetails.class, () -> () -> CONNECTION_STRING)
            .run(context -> {
                assertThat(context).hasSingleBean(StaticConnectionStringProvider.class);
                StaticConnectionStringProvider<?> provider = context.getBean(StaticConnectionStringProvider.class);
                assertThat(provider.getServiceType()).isEqualTo(AzureServiceType.EVENT_HUBS);
                assertThat(provider.getConnectionString()).isEqualTo(CONNECTION_STRING);
            });
    }

    @Test
    void namespaceOnlyDoesNotRegisterProvider() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventhubs.namespace=test-namespace")
            .run(context -> assertThat(context).doesNotHaveBean(StaticConnectionStringProvider.class));
    }
}
