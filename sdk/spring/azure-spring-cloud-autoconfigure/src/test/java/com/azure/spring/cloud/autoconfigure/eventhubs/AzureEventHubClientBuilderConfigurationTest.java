// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.eventhubs.factory.EventHubClientBuilderFactory;
import com.azure.spring.core.StaticConnectionStringProvider;
import com.azure.spring.core.service.AzureServiceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.azure.spring.cloud.autoconfigure.eventhubs.EventHubsTestUtils.CONNECTION_STRING;
import static org.assertj.core.api.Assertions.assertThat;

class AzureEventHubClientBuilderConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubClientBuilderConfiguration.class));

    @Test
    void noConnectionInfoProvidedShouldNotConfigure() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(AzureEventHubClientBuilderConfiguration.class));
    }

    @Test
    @SuppressWarnings("rawtypes")
    void connectionStringProvidedShouldConfigure() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING, "test-namespace"),
                "spring.cloud.azure.eventhubs.event-hub-name=test-event-hub"
            )
            .withUserConfiguration(AzureEventHubPropertiesTestConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubClientBuilderConfiguration.class);
                assertThat(context).hasSingleBean(EventHubClientBuilderFactory.class);
                assertThat(context).hasSingleBean(EventHubClientBuilder.class);
                assertThat(context).hasSingleBean(StaticConnectionStringProvider.class);
                StaticConnectionStringProvider connectionStringProvider = context.getBean(StaticConnectionStringProvider.class);
                Assertions.assertEquals(AzureServiceType.EVENT_HUB, connectionStringProvider.getServiceType());

            });
    }

}
