// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.spring.eventhubs.core.EventHubsTemplate;
import com.azure.spring.eventhubs.core.producer.EventHubProducerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.azure.spring.cloud.autoconfigure.eventhubs.EventHubsTestUtils.CONNECTION_STRING;
import static org.assertj.core.api.Assertions.assertThat;

class AzureEventHubsTemplateConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubMessagingAutoConfiguration.class));

    @Test
    void disableEventHubsShouldNotConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.enabled=false",
                "spring.cloud.azure.eventhubs.namespace=test-namespace"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubMessagingAutoConfiguration.EventHubsTemplateConfiguration.class));
    }

    @Test
    void withoutEventHubsTemplateShouldNotConfigure() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(EventHubsTemplate.class))
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.namespace=test-namespace"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubMessagingAutoConfiguration.EventHubsTemplateConfiguration.class));
    }

    @Test
    void withoutEventHubConnectionShouldNotConfigure() {
        this.contextRunner
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubMessagingAutoConfiguration.EventHubsTemplateConfiguration.class));
    }

    @Test
    void connectionInfoProvidedShouldConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING, "test-namespace")
            )
            .withUserConfiguration(AzureEventHubPropertiesTestConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(EventHubsTemplate.class);
                assertThat(context).hasSingleBean(EventHubProducerFactory.class);
                assertThat(context).hasSingleBean(AzureEventHubMessagingAutoConfiguration.EventHubsTemplateConfiguration.class);
            });
    }

}
