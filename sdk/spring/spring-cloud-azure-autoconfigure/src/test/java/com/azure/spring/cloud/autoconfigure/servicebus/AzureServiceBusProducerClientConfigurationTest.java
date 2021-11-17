// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.spring.service.servicebus.factory.ServiceBusSenderClientBuilderFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.azure.spring.cloud.autoconfigure.servicebus.ServiceBusTestUtils.CONNECTION_STRING;
import static org.assertj.core.api.Assertions.assertThat;

class AzureServiceBusProducerClientConfigurationTest {


    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusProducerClientConfiguration.class));

    @Test
    void noEntityNameProvidedShouldNotConfigure() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.entity-type=queue",
                "spring.cloud.azure.servicebus.producer.entity-type=queue"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusProducerClientConfiguration.class));
    }

    @Test
    void noEntityTypeProvidedShouldNotConfigure() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.entity-name=queue",
                "spring.cloud.azure.servicebus.producer.entity-name=queue"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusProducerClientConfiguration.class);
                assertThat(context).doesNotHaveBean(ServiceBusSenderClientBuilderFactory.class);
            });
    }

    @Test
    void entityNameAndTypeProvidedShouldConfigure() {
        ServiceBusClientBuilder serviceBusClientBuilder = new ServiceBusClientBuilder();
        serviceBusClientBuilder.connectionString(String.format(CONNECTION_STRING, "test-namespace"));

        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.entity-name=test-queue",
                "spring.cloud.azure.servicebus.entity-type=queue"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(ServiceBusClientBuilder.class, () -> serviceBusClientBuilder)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusProducerClientConfiguration.class);
                assertThat(context).hasSingleBean(ServiceBusSenderClient.class);
            });
    }


    @Test
    void dedicatedConnectionInfoProvidedShouldConfigureDedicated() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.producer.entity-name=test-queue",
                "spring.cloud.azure.servicebus.producer.entity-type=topic",
                "spring.cloud.azure.servicebus.producer.connection-string=" + String.format(CONNECTION_STRING, "test-namespace")
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusProducerClientConfiguration.class);
                assertThat(context).hasSingleBean(ServiceBusSenderClient.class);
            });
    }

}
