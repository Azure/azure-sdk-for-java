// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.spring.cloud.autoconfigure.context.AzureContextUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.azure.spring.cloud.autoconfigure.servicebus.ServiceBusTestUtils.CONNECTION_STRING;
import static org.assertj.core.api.Assertions.assertThat;

class AzureServiceBusProducerClientConfigurationTest {


    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusProducerClientConfiguration.class));

    @Test
    void noQueueNameOrTopicNameProvidedShouldNotConfigure() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusProducerClientConfiguration.class));
    }

    @Test
    void queueNameProvidedShouldConfigure() {
        ServiceBusClientBuilder serviceBusClientBuilder = new ServiceBusClientBuilder();
        serviceBusClientBuilder.connectionString(String.format(CONNECTION_STRING, "test-namespace"));

        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.producer.queue-name=test-queue"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(ServiceBusClientBuilder.class, () -> serviceBusClientBuilder)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusProducerClientConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusProducerClientConfiguration.SharedProducerConnectionConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusProducerClientConfiguration.DedicatedProducerConnectionConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusProducerClientConfiguration.ProducerClientConfiguration.class);
                assertThat(context).hasSingleBean(ServiceBusSenderClient.class);
            });
    }

    @Test
    void topicNameProvidedShouldConfigure() {
        ServiceBusClientBuilder serviceBusClientBuilder = new ServiceBusClientBuilder();
        serviceBusClientBuilder.connectionString(String.format(CONNECTION_STRING, "test-namespace"));

        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.producer.queue-name=test-queue"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(ServiceBusClientBuilder.class, () -> serviceBusClientBuilder)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusProducerClientConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusProducerClientConfiguration.SharedProducerConnectionConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusProducerClientConfiguration.DedicatedProducerConnectionConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusProducerClientConfiguration.ProducerClientConfiguration.class);
                assertThat(context).hasSingleBean(ServiceBusSenderClient.class);
            });
    }

    @Test
    void dedicatedConnectionInfoProvidedShouldConfigureDedicated() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.producer.queue-name=test-queue",
                "spring.cloud.azure.servicebus.producer.connection-string=" + String.format(CONNECTION_STRING, "test-namespace")
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusProducerClientConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusProducerClientConfiguration.DedicatedProducerConnectionConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusProducerClientConfiguration.SharedProducerConnectionConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusProducerClientConfiguration.ProducerClientConfiguration.class);
                assertThat(context).hasSingleBean(ServiceBusSenderClient.class);

                assertThat(context).hasBean(AzureContextUtils.SERVICE_BUS_PRODUCER_CLIENT_BUILDER_BEAN_NAME);
                assertThat(context).hasBean(AzureContextUtils.SERVICE_BUS_PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME);
            });
    }

}
