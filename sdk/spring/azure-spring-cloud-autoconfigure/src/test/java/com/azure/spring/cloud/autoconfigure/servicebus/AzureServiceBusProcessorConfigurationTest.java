// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.spring.cloud.autoconfigure.context.AzureContextUtils;
import com.azure.spring.servicebus.core.ServiceBusMessageProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.function.Consumer;

import static com.azure.spring.cloud.autoconfigure.servicebus.ServiceBusTestUtils.CONNECTION_STRING;
import static org.assertj.core.api.Assertions.assertThat;

class AzureServiceBusProcessorConfigurationTest {


    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusProcessorConfiguration.class));

    @Test
    void noQueueNameOrTopicNameProvidedShouldNotConfigure() {
        contextRunner
            .withBean(ServiceBusMessageProcessor.class, ServiceBusMessageProcessorTestImpl::new)
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusProcessorConfiguration.class));
    }

    @Test
    void noMessageProcessorShouldNotConfigure() {
        contextRunner
            .withPropertyValues("spring.cloud.azure.servicebus.processor.queue-name=test-queue")
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusProcessorConfiguration.class));
    }

    @Test
    void queueNameAndMessageProcessorProvidedShouldConfigure() {
        ServiceBusClientBuilder serviceBusClientBuilder = new ServiceBusClientBuilder();
        serviceBusClientBuilder.connectionString(String.format(CONNECTION_STRING, "test-namespace"));

        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.processor.queue-name=test-queue"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(ServiceBusMessageProcessor.class, ServiceBusMessageProcessorTestImpl::new)
            .withBean(ServiceBusClientBuilder.class, () -> serviceBusClientBuilder)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusProcessorConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusProcessorConfiguration.ShareProcessorConnectionConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusProcessorConfiguration.DedicatedProcessorConnectionConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusProcessorConfiguration.NoneSessionProcessorClientConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusProcessorConfiguration.SessionProcessorClientConfiguration.class);
            });
    }

    @Test
    void topicNameAndMessageProcessorProvidedShouldConfigure() {
        ServiceBusClientBuilder serviceBusClientBuilder = new ServiceBusClientBuilder();
        serviceBusClientBuilder.connectionString(String.format(CONNECTION_STRING, "test-namespace"));

        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.processor.topic-name=test-queue",
                "spring.cloud.azure.servicebus.processor.subscription-name=test-sub"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(ServiceBusMessageProcessor.class, ServiceBusMessageProcessorTestImpl::new)
            .withBean(ServiceBusClientBuilder.class, () -> serviceBusClientBuilder)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusProcessorConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusProcessorConfiguration.ShareProcessorConnectionConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusProcessorConfiguration.DedicatedProcessorConnectionConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusProcessorConfiguration.NoneSessionProcessorClientConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusProcessorConfiguration.SessionProcessorClientConfiguration.class);
            });
    }

    @Test
    void dedicatedConnectionInfoProvidedShouldConfigureDedicated() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.processor.queue-name=test-queue",
                "spring.cloud.azure.servicebus.processor.connection-string=" + String.format(CONNECTION_STRING, "test-namespace")
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(ServiceBusMessageProcessor.class, ServiceBusMessageProcessorTestImpl::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusProcessorConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusProcessorConfiguration.DedicatedProcessorConnectionConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusProcessorConfiguration.ShareProcessorConnectionConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusProcessorConfiguration.NoneSessionProcessorClientConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusProcessorConfiguration.SessionProcessorClientConfiguration.class);

                assertThat(context).doesNotHaveBean(ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder.class);
                assertThat(context).hasSingleBean(ServiceBusClientBuilder.ServiceBusProcessorClientBuilder.class);
                assertThat(context).hasSingleBean(ServiceBusProcessorClient.class);

                assertThat(context).hasBean(AzureContextUtils.SERVICE_BUS_PROCESSOR_CLIENT_BUILDER_BEAN_NAME);
                assertThat(context).hasBean(AzureContextUtils.SERVICE_BUS_PROCESSOR_CLIENT_BUILDER_FACTORY_BEAN_NAME);
            });
    }

    @Test
    void sessionAwareEnabledShouldConfigureSession() {
        ServiceBusClientBuilder serviceBusClientBuilder = new ServiceBusClientBuilder();
        serviceBusClientBuilder.connectionString(String.format(CONNECTION_STRING, "test-namespace"));

        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.processor.queue-name=test-queue",
                "spring.cloud.azure.servicebus.processor.session-aware=true"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(ServiceBusMessageProcessor.class, ServiceBusMessageProcessorTestImpl::new)
            .withBean(ServiceBusClientBuilder.class, () -> serviceBusClientBuilder)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusProcessorConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusProcessorConfiguration.ShareProcessorConnectionConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusProcessorConfiguration.DedicatedProcessorConnectionConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusProcessorConfiguration.SessionProcessorClientConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusProcessorConfiguration.NoneSessionProcessorClientConfiguration.class);

                assertThat(context).doesNotHaveBean(ServiceBusClientBuilder.ServiceBusProcessorClientBuilder.class);
                assertThat(context).hasSingleBean(ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder.class);
                assertThat(context).hasSingleBean(ServiceBusProcessorClient.class);
            });
    }

    @Test
    void sessionAwareEnabledWithDedicatedConnectionShouldConfigureSession() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.processor.queue-name=test-queue",
                "spring.cloud.azure.servicebus.processor.connection-string=" + String.format(CONNECTION_STRING, "test-namespace"),
                "spring.cloud.azure.servicebus.processor.session-aware=true"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(ServiceBusMessageProcessor.class, ServiceBusMessageProcessorTestImpl::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusProcessorConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusProcessorConfiguration.DedicatedProcessorConnectionConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusProcessorConfiguration.ShareProcessorConnectionConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusProcessorConfiguration.SessionProcessorClientConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusProcessorConfiguration.NoneSessionProcessorClientConfiguration.class);

                assertThat(context).doesNotHaveBean(ServiceBusClientBuilder.ServiceBusProcessorClientBuilder.class);
                assertThat(context).hasSingleBean(ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder.class);
                assertThat(context).hasSingleBean(ServiceBusProcessorClient.class);
            });
    }

    static class ServiceBusMessageProcessorTestImpl implements ServiceBusMessageProcessor {

        @Override
        public Consumer<ServiceBusErrorContext> processError() {
            return errorContext -> { };
        }

        @Override
        public Consumer<ServiceBusReceivedMessageContext> processMessage() {
            return messageContext -> { };
        }
    }

}

