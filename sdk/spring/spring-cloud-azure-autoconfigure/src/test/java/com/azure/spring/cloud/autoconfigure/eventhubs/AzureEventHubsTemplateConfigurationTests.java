// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.spring.messaging.eventhubs.core.EventHubsProducerFactory;
import com.azure.spring.messaging.eventhubs.core.EventHubsTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.azure.spring.cloud.autoconfigure.eventhubs.EventHubsTestUtils.CONNECTION_STRING_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;

class AzureEventHubsTemplateConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubsMessagingAutoConfiguration.class));

    @Test
    void disableEventHubsShouldNotConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.enabled=false",
                "spring.cloud.azure.eventhubs.namespace=test-namespace"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsMessagingAutoConfiguration.EventHubsTemplateConfiguration.class));
    }

    @Test
    void withoutEventHubsTemplateShouldNotConfigure() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(EventHubsTemplate.class))
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.namespace=test-namespace"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsMessagingAutoConfiguration.EventHubsTemplateConfiguration.class));
    }

    @Test
    void withoutEventHubConnectionShouldNotConfigure() {
        this.contextRunner
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsMessagingAutoConfiguration.EventHubsTemplateConfiguration.class));
    }

    @Test
    void connectionInfoProvidedShouldConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withUserConfiguration(AzureEventHubsPropertiesTestConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(EventHubsTemplate.class);
                assertThat(context).hasSingleBean(EventHubsProducerFactory.class);
                assertThat(context).hasSingleBean(AzureEventHubsMessagingAutoConfiguration.EventHubsTemplateConfiguration.class);
            });
    }

}
