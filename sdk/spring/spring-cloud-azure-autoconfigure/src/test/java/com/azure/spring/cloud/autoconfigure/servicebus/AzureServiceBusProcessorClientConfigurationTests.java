// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.cloud.autoconfigure.TestBuilderCustomizer;
import com.azure.spring.cloud.service.implementation.servicebus.factory.ServiceBusSessionProcessorClientBuilderFactory;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusErrorHandler;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusRecordMessageListener;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.azure.spring.cloud.autoconfigure.servicebus.ServiceBusTestUtils.CONNECTION_STRING_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AzureServiceBusProcessorClientConfigurationTests {


    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusProcessorClientConfiguration.class));

    @Test
    void noMessageListenerAndErrorHandlerShouldNotConfigure() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.entity-name=test-queue",
                "spring.cloud.azure.servicebus.entity-type=queue"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusProcessorClientConfiguration.class));
    }

    @Test
    void noMessageListenerShouldNotConfigure() {
        contextRunner
            .withBean(ServiceBusErrorHandler.class, () -> errorContext -> { })
            .withPropertyValues(
                "spring.cloud.azure.servicebus.entity-name=test-queue",
                "spring.cloud.azure.servicebus.entity-type=queue"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusProcessorClientConfiguration.class));
    }

    @Test
    void noErrorHandlerShouldNotConfigure() {
        contextRunner
            .withBean(ServiceBusRecordMessageListener.class, () -> messageContext -> { })
            .withPropertyValues(
                "spring.cloud.azure.servicebus.entity-name=test-queue",
                "spring.cloud.azure.servicebus.entity-type=queue"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusProcessorClientConfiguration.class));
    }

    @Test
    void noEntityTypeProvidedShouldNotConfigure() {
        contextRunner
            .withBean(ServiceBusRecordMessageListener.class, () -> messageContext -> { })
            .withBean(ServiceBusErrorHandler.class, () -> errorContext -> { })
            .withPropertyValues(
                "spring.cloud.azure.servicebus.entity-name=test-queue",
                "spring.cloud.azure.servicebus.processor.entity-name=test-queue"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusProcessorClientConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusProcessorClientConfiguration.NoneSessionProcessorClientConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusProcessorClientConfiguration.SessionProcessorClientConfiguration.class);
            });
    }

    @Test
    void noEntityNameProvidedShouldNotConfigure() {
        contextRunner
            .withBean(ServiceBusRecordMessageListener.class, () -> messageContext -> { })
            .withBean(ServiceBusErrorHandler.class, () -> errorContext -> { })
            .withPropertyValues(
                "spring.cloud.azure.servicebus.entity-type=queue",
                "spring.cloud.azure.servicebus.processor.entity-type=queue"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusProcessorClientConfiguration.class));
    }

    @Test
    void noTopicSubscriptionProvidedShouldNotConfigure() {
        contextRunner
            .withBean(ServiceBusRecordMessageListener.class, () -> messageContext -> { })
            .withBean(ServiceBusErrorHandler.class, () -> errorContext -> { })
            .withPropertyValues(
                "spring.cloud.azure.servicebus.processor.entity-name=test-topic",
                "spring.cloud.azure.servicebus.processor.entity-type=topic"
            )
            .run(context -> assertThrows(IllegalStateException.class, () -> context.getBean(AzureServiceBusProcessorClientConfiguration.class)));
    }

    @Test
    void queueNameAndMessageProcessorProvidedShouldConfigure() {
        ServiceBusClientBuilder serviceBusClientBuilder = new ServiceBusClientBuilder();
        serviceBusClientBuilder.connectionString(String.format(CONNECTION_STRING_FORMAT, "test-namespace"));

        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.entity-name=test-queue",
                "spring.cloud.azure.servicebus.entity-type=queue"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(ServiceBusRecordMessageListener.class, () -> messageContext -> { })
            .withBean(ServiceBusErrorHandler.class, () -> errorContext -> { })
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
        serviceBusClientBuilder.connectionString(String.format(CONNECTION_STRING_FORMAT, "test-namespace"));

        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.entity-name=test-topic",
                "spring.cloud.azure.servicebus.processor.subscription-name=test-sub",
                "spring.cloud.azure.servicebus.entity-type=topic"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(ServiceBusRecordMessageListener.class, () -> messageContext -> { })
            .withBean(ServiceBusErrorHandler.class, () -> errorContext -> { })
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
                "spring.cloud.azure.servicebus.processor.entity-name=test-queue",
                "spring.cloud.azure.servicebus.processor.entity-type=queue",
                "spring.cloud.azure.servicebus.processor.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(ServiceBusRecordMessageListener.class, () -> messageContext -> { })
            .withBean(ServiceBusErrorHandler.class, () -> errorContext -> { })
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
    void sessionEnabledShouldConfigureSession() {
        ServiceBusClientBuilder serviceBusClientBuilder = new ServiceBusClientBuilder();
        serviceBusClientBuilder.connectionString(String.format(CONNECTION_STRING_FORMAT, "test-namespace"));

        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.processor.entity-name=test-topic",
                "spring.cloud.azure.servicebus.processor.subscription-name=test-sub",
                "spring.cloud.azure.servicebus.processor.entity-type=topic",
                "spring.cloud.azure.servicebus.processor.session-enabled=true"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(ServiceBusRecordMessageListener.class, () -> messageContext -> { })
            .withBean(ServiceBusErrorHandler.class, () -> errorContext -> { })
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
    void sessionEnabledWithDedicatedConnectionShouldConfigureSession() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.processor.entity-name=test-queue",
                "spring.cloud.azure.servicebus.processor.entity-type=queue",
                "spring.cloud.azure.servicebus.processor.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace"),
                "spring.cloud.azure.servicebus.processor.session-enabled=true"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(ServiceBusRecordMessageListener.class, () -> messageContext -> { })
            .withBean(ServiceBusErrorHandler.class, () -> errorContext -> { })
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusProcessorClientConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusProcessorClientConfiguration.SessionProcessorClientConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusProcessorClientConfiguration.NoneSessionProcessorClientConfiguration.class);

                assertThat(context).doesNotHaveBean(ServiceBusClientBuilder.ServiceBusProcessorClientBuilder.class);
                assertThat(context).hasSingleBean(ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder.class);
                assertThat(context).hasSingleBean(ServiceBusProcessorClient.class);
            });
    }

    @Test
    void customizerShouldBeCalledForSession() {
        ServiceBusSessionProcessorClientBuilderCustomizer customizer = new ServiceBusSessionProcessorClientBuilderCustomizer();
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.processor.entity-name=test-queue",
                "spring.cloud.azure.servicebus.processor.entity-type=queue",
                "spring.cloud.azure.servicebus.processor.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace"),
                "spring.cloud.azure.servicebus.processor.session-enabled=true"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(ServiceBusRecordMessageListener.class, () -> messageContext -> { })
            .withBean(ServiceBusErrorHandler.class, () -> errorContext -> { })
            .withBean("customizer1", ServiceBusSessionProcessorClientBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", ServiceBusSessionProcessorClientBuilderCustomizer.class, () -> customizer)
            .run(context -> assertThat(customizer.getCustomizedTimes()).isEqualTo(2));
    }

    @Test
    void otherCustomizerShouldNotBeCalled() {
        ServiceBusProcessorClientBuilderCustomizer customizer = new ServiceBusProcessorClientBuilderCustomizer();
        OtherBuilderCustomizer otherBuilderCustomizer = new OtherBuilderCustomizer();
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.processor.entity-name=test-queue",
                "spring.cloud.azure.servicebus.processor.entity-type=queue",
                "spring.cloud.azure.servicebus.processor.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(ServiceBusRecordMessageListener.class, () -> messageContext -> { })
            .withBean(ServiceBusErrorHandler.class, () -> errorContext -> { })
            .withBean("customizer1", ServiceBusProcessorClientBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", ServiceBusProcessorClientBuilderCustomizer.class, () -> customizer)
            .withBean("customizer3", OtherBuilderCustomizer.class, () -> otherBuilderCustomizer)
            .run(context -> {
                assertThat(customizer.getCustomizedTimes()).isEqualTo(2);
                assertThat(otherBuilderCustomizer.getCustomizedTimes()).isEqualTo(0);
            });
    }

    private static class ServiceBusProcessorClientBuilderCustomizer extends TestBuilderCustomizer<ServiceBusClientBuilder.ServiceBusProcessorClientBuilder> {

    }
    private static class ServiceBusSessionProcessorClientBuilderCustomizer extends TestBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder> {

    }

    private static class OtherBuilderCustomizer extends TestBuilderCustomizer<ConfigurationClientBuilder> {

    }

}

