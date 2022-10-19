// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.spring.messaging.servicebus.core.ServiceBusProcessorFactory;
import com.azure.spring.messaging.servicebus.core.ServiceBusTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.azure.spring.cloud.autoconfigure.servicebus.ServiceBusTestUtils.CONNECTION_STRING_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;

class AzureServiceBusMessagingAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusMessagingAutoConfiguration.class));

    @Test
    void disableServiceBusShouldNotConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.enabled=false",
                "spring.cloud.azure.servicebus.namespace=test-namespace"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusMessagingAutoConfiguration.ProcessorContainerConfiguration.class));
    }

    @Test
    void withoutServiceBusTemplateShouldNotConfigure() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(ServiceBusTemplate.class))
            .withPropertyValues(
                "spring.cloud.azure.servicebus.namespace=test-namespace"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusMessagingAutoConfiguration.ProcessorContainerConfiguration.class));
    }

    @Test
    void withoutServiceBusConnectionShouldNotConfigure() {
        this.contextRunner
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusMessagingAutoConfiguration.ProcessorContainerConfiguration.class));
    }

    @Test
    void connectionInfoAndCheckpointStoreProvidedShouldConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(ServiceBusProcessorFactory.class);
                assertThat(context).hasSingleBean(AzureServiceBusMessagingAutoConfiguration.ProcessorContainerConfiguration.class);
            });
    }


}
