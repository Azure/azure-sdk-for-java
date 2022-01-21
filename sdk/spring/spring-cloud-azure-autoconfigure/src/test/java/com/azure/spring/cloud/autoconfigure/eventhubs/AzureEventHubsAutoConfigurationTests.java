// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.core.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.core.service.AzureServiceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;

import static com.azure.spring.cloud.autoconfigure.eventhubs.EventHubsTestUtils.CONNECTION_STRING_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;

class AzureEventHubsAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubsAutoConfiguration.class));

    @Test
    void configureWithoutEventHubClientBuilder() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(EventHubClientBuilder.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsAutoConfiguration.class));
    }

    @Test
    void configureWithEventHubDisabled() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventhubs.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsAutoConfiguration.class));
    }

    @Test
    void configureWithoutConnectionStringAndNamespace() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventhubs.enabled=true")
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsAutoConfiguration.class));
    }

    @Test
    void configureWithNamespace() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventhubs.namespace=test-eventhub-namespace")
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> assertThat(context).hasSingleBean(AzureEventHubsProperties.class));
    }

    @Test
    void configureWithConnectionString() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventhubs.connection-string=test-connection-string")
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> assertThat(context).hasSingleBean(AzureEventHubsProperties.class));
    }

    @Test
    void configureAzureEventHubsProperties() {
        AzureGlobalProperties azureProperties = new AzureGlobalProperties();
        azureProperties.getCredential().setClientId("azure-client-id");
        azureProperties.getCredential().setClientSecret("azure-client-secret");
        azureProperties.getRetry().getBackoff().setDelay(Duration.ofSeconds(2));

        this.contextRunner
            .withBean(AzureGlobalProperties.class, () -> azureProperties)
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.credential.client-id=eventhubs-client-id",
                "spring.cloud.azure.eventhubs.retry.backoff.delay=2m",
                "spring.cloud.azure.eventhubs.connection-string=test-connection-string"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubsProperties.class);
                final AzureEventHubsProperties properties = context.getBean(AzureEventHubsProperties.class);
                assertThat(properties).extracting("credential.clientId").isEqualTo("eventhubs-client-id");
                assertThat(properties).extracting("credential.clientSecret").isEqualTo("azure-client-secret");
                assertThat(properties).extracting("retry.backoff.delay").isEqualTo(Duration.ofMinutes(2));
                assertThat(properties).extracting("connectionString").isEqualTo("test-connection-string");

                assertThat(azureProperties.getCredential().getClientId()).isEqualTo("azure-client-id");
            });
    }

    @Test
    @SuppressWarnings("rawtypes")
    void connectionStringProvidedShouldConfigureConnectionProvider() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace"),
                "spring.cloud.azure.eventhubs.event-hub-name=test-event-hub"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubsAutoConfiguration.class);
                assertThat(context).hasSingleBean(StaticConnectionStringProvider.class);
                StaticConnectionStringProvider connectionStringProvider = context.getBean(StaticConnectionStringProvider.class);
                Assertions.assertEquals(AzureServiceType.EVENT_HUBS, connectionStringProvider.getServiceType());
            });
    }

}
