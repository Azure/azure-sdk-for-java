// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.eventhubs.properties.AzureEventHubProperties;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AzureEventHubAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubAutoConfiguration.class));

    @Test
    void configureWithoutEventHubClientBuilder() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(EventHubClientBuilder.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubAutoConfiguration.class));
    }

    @Test
    void configureWithEventHubDisabled() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventhubs.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubAutoConfiguration.class));
    }

    @Test
    void configureWithoutConnectionStringAndNamespace() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventhubs.enabled=true")
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubAutoConfiguration.class));
    }

    @Test
    void configureWithNamespace() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventhubs.namespace=test-eventhub-namespace")
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> assertThat(context).hasSingleBean(AzureEventHubProperties.class));
    }

    @Test
    void configureWithConnectionString() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventhubs.connection-string=test-connection-string")
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> assertThat(context).hasSingleBean(AzureEventHubProperties.class));
    }

}
