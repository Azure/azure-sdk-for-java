// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.spring.service.servicebus.factory.ServiceBusSessionProcessorClientBuilderFactory;
import com.azure.spring.service.servicebus.processor.MessageProcessingListener;
import com.azure.spring.service.servicebus.processor.RecordMessageProcessingListener;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.azure.spring.cloud.autoconfigure.servicebus.ServiceBusTestUtils.CONNECTION_STRING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AzureServiceBusProcessorClientConfigurationTest {


    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusProcessorClientConfiguration.class));

    @Test
    void noEntityTypeProvidedShouldNotConfigure() {
        contextRunner
            .withBean(MessageProcessingListener.class, TestMessageProcessingListener::new)
            .withPropertyValues(
                "spring.cloud.azure.servicebus.processor.name=test-queue"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusProcessorClientConfiguration.class));
    }

    @Test
    void noEntityNameProvidedShouldNotConfigure() {
        contextRunner
            .withBean(MessageProcessingListener.class, TestMessageProcessingListener::new)
            .withPropertyValues(
                "spring.cloud.azure.servicebus.processor.type=queue"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusProcessorClientConfiguration.class));
    }

    @Test
    void noTopicSubscriptionProvidedShouldNotConfigure() {
        contextRunner
            .withBean(MessageProcessingListener.class, TestMessageProcessingListener::new)
            .withPropertyValues(
                "spring.cloud.azure.servicebus.processor.name=test-topic",
                "spring.cloud.azure.servicebus.processor.type=topic"
            )
            .run(context -> assertThrows(IllegalStateException.class, () -> context.getBean(AzureServiceBusProcessorClientConfiguration.class)));
    }

    @Test
    void noMessageProcessorShouldNotConfigure() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.processor.name=test-queue",
                "spring.cloud.azure.servicebus.processor.type=queue"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusProcessorClientConfiguration.class));
    }

    @Test
    void queueNameAndMessageProcessorProvidedShouldConfigure() {
        ServiceBusClientBuilder serviceBusClientBuilder = new ServiceBusClientBuilder();
        serviceBusClientBuilder.connectionString(String.format(CONNECTION_STRING, "test-namespace"));

        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.processor.name=test-queue",
                "spring.cloud.azure.servicebus.processor.type=queue"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(MessageProcessingListener.class, TestMessageProcessingListener::new)
            .withBean(ServiceBusClientBuilder.class, () -> serviceBusClientBuilder)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusProcessorClientConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusProcessorClientConfiguration.NoneSessionProcessorClientConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusProcessorClientConfiguration.SessionProcessorClientConfiguration.class);
            });
    }

    @Test
    void topicNameAndMessageProcessorProvidedShouldConfigure() {
        ServiceBusClientBuilder serviceBusClientBuilder = new ServiceBusClientBuilder();
        serviceBusClientBuilder.connectionString(String.format(CONNECTION_STRING, "test-namespace"));

        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.processor.name=test-topic",
                "spring.cloud.azure.servicebus.processor.subscription-name=test-sub",
                "spring.cloud.azure.servicebus.processor.type=topic"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(MessageProcessingListener.class, TestMessageProcessingListener::new)
            .withBean(ServiceBusClientBuilder.class, () -> serviceBusClientBuilder)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusProcessorClientConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusProcessorClientConfiguration.NoneSessionProcessorClientConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusProcessorClientConfiguration.SessionProcessorClientConfiguration.class);
            });
    }

    @Test
    void dedicatedConnectionInfoProvidedShouldConfigureDedicated() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.processor.name=test-queue",
                "spring.cloud.azure.servicebus.processor.type=queue",
                "spring.cloud.azure.servicebus.processor.connection-string=" + String.format(CONNECTION_STRING, "test-namespace")
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(MessageProcessingListener.class, TestMessageProcessingListener::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusProcessorClientConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusProcessorClientConfiguration.NoneSessionProcessorClientConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusProcessorClientConfiguration.SessionProcessorClientConfiguration.class);

                assertThat(context).doesNotHaveBean(ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder.class);
                assertThat(context).hasSingleBean(ServiceBusClientBuilder.ServiceBusProcessorClientBuilder.class);
                assertThat(context).hasSingleBean(ServiceBusProcessorClient.class);
            });
    }

    @Test
    void sessionAwareEnabledShouldConfigureSession() {
        ServiceBusClientBuilder serviceBusClientBuilder = new ServiceBusClientBuilder();
        serviceBusClientBuilder.connectionString(String.format(CONNECTION_STRING, "test-namespace"));

        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.processor.name=test-topic",
                "spring.cloud.azure.servicebus.processor.subscription-name=test-sub",
                "spring.cloud.azure.servicebus.processor.type=topic",
                "spring.cloud.azure.servicebus.processor.session-aware=true"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(MessageProcessingListener.class, TestMessageProcessingListener::new)
            .withBean(ServiceBusClientBuilder.class, () -> serviceBusClientBuilder)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusProcessorClientConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusProcessorClientConfiguration.SessionProcessorClientConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusProcessorClientConfiguration.NoneSessionProcessorClientConfiguration.class);

                assertThat(context).hasSingleBean(ServiceBusSessionProcessorClientBuilderFactory.class);
                assertThat(context).doesNotHaveBean(ServiceBusClientBuilder.ServiceBusProcessorClientBuilder.class);
                assertThat(context).hasSingleBean(ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder.class);
                assertThat(context).hasSingleBean(ServiceBusProcessorClient.class);
            });
    }

    @Test
    void sessionAwareEnabledWithDedicatedConnectionShouldConfigureSession() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.processor.name=test-queue",
                "spring.cloud.azure.servicebus.processor.type=queue",
                "spring.cloud.azure.servicebus.processor.connection-string=" + String.format(CONNECTION_STRING, "test-namespace"),
                "spring.cloud.azure.servicebus.processor.session-aware=true"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(MessageProcessingListener.class, TestMessageProcessingListener::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusProcessorClientConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusProcessorClientConfiguration.SessionProcessorClientConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusProcessorClientConfiguration.NoneSessionProcessorClientConfiguration.class);

                assertThat(context).doesNotHaveBean(ServiceBusClientBuilder.ServiceBusProcessorClientBuilder.class);
                assertThat(context).hasSingleBean(ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder.class);
                assertThat(context).hasSingleBean(ServiceBusProcessorClient.class);
            });
    }

    static class TestMessageProcessingListener implements RecordMessageProcessingListener {

        @Override
        public void onMessage(ServiceBusReceivedMessageContext messageContext) {

        }
    }

}

