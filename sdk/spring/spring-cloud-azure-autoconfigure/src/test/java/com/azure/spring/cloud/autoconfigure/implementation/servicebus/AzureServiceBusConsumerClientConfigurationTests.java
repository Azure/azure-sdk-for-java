// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.servicebus;

import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverClient;
import com.azure.spring.cloud.autoconfigure.implementation.TestBuilderCustomizer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.azure.spring.cloud.autoconfigure.implementation.servicebus.ServiceBusTestUtils.CONNECTION_STRING_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;

class AzureServiceBusConsumerClientConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusConsumerClientConfiguration.class));

    @Test
    void noQueueNameOrTopicNameProvidedShouldNotConfigure() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusConsumerClientConfiguration.class));
    }

    @Test
    void entityNameProvidedShouldConfigure() {
        ServiceBusClientBuilder serviceBusClientBuilder = new ServiceBusClientBuilder();
        serviceBusClientBuilder.connectionString(String.format(CONNECTION_STRING_FORMAT, "test-namespace"));

        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.entity-type=queue",
                "spring.cloud.azure.servicebus.consumer.entity-type=queue"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(ServiceBusClientBuilder.class, () -> serviceBusClientBuilder)
            .run(context -> {
                assertThat(context).doesNotHaveBean(AzureServiceBusConsumerClientConfiguration.class);
            });
    }

    @Test
    void queueNameProvidedShouldConfigure() {
        ServiceBusClientBuilder serviceBusClientBuilder = new ServiceBusClientBuilder();
        serviceBusClientBuilder.connectionString(String.format(CONNECTION_STRING_FORMAT, "test-namespace"));

        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.entity-name=test-queue",
                "spring.cloud.azure.servicebus.entity-type=queue"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(ServiceBusClientBuilder.class, () -> serviceBusClientBuilder)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusConsumerClientConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusConsumerClientConfiguration.NoneSessionConsumerClientConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusConsumerClientConfiguration.SessionConsumerClientConfiguration.class);
            });
    }

    @Test
    void subscriptionNameProvidedShouldConfigure() {
        ServiceBusClientBuilder serviceBusClientBuilder = new ServiceBusClientBuilder();
        serviceBusClientBuilder.connectionString(String.format(CONNECTION_STRING_FORMAT, "test-namespace"));

        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.consumer.entity-name=test-topic",
                "spring.cloud.azure.servicebus.consumer.subscription-name=test-sub",
                "spring.cloud.azure.servicebus.consumer.entity-type=topic"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(ServiceBusClientBuilder.class, () -> serviceBusClientBuilder)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusConsumerClientConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusConsumerClientConfiguration.NoneSessionConsumerClientConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusConsumerClientConfiguration.SessionConsumerClientConfiguration.class);
            });
    }

    @Test
    void noSubscriptionNameShouldConfigureForQueue() {
        ServiceBusClientBuilder serviceBusClientBuilder = new ServiceBusClientBuilder();
        serviceBusClientBuilder.connectionString(String.format(CONNECTION_STRING_FORMAT, "test-namespace"));

        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.entity-name=test-queue",
                "spring.cloud.azure.servicebus.entity-type=queue"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(ServiceBusClientBuilder.class, () -> serviceBusClientBuilder)
            .run(context -> assertThat(context).hasSingleBean(AzureServiceBusConsumerClientConfiguration.class));
    }

    @Test
    void noSubscriptionNameShouldNotConfigureForTopic() {
        ServiceBusClientBuilder serviceBusClientBuilder = new ServiceBusClientBuilder();
        serviceBusClientBuilder.connectionString(String.format(CONNECTION_STRING_FORMAT, "test-namespace"));

        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.entity-name=test-topic",
                "spring.cloud.azure.servicebus.entity-type=topic"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(ServiceBusClientBuilder.class, () -> serviceBusClientBuilder)
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusConsumerClientConfiguration.class));
    }

    @Test
    void subscriptionNameShouldConfigureForTopic() {
        ServiceBusClientBuilder serviceBusClientBuilder = new ServiceBusClientBuilder();
        serviceBusClientBuilder.connectionString(String.format(CONNECTION_STRING_FORMAT, "test-namespace"));

        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.entity-name=test-topic",
                "spring.cloud.azure.servicebus.entity-type=topic",
                "spring.cloud.azure.servicebus.consumer.subscription-name=test-sub"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(ServiceBusClientBuilder.class, () -> serviceBusClientBuilder)
            .run(context -> assertThat(context).hasSingleBean(AzureServiceBusConsumerClientConfiguration.class));
    }

    @Test
    void subscriptionNameProvidedShouldNotConfigure() {
        ServiceBusClientBuilder serviceBusClientBuilder = new ServiceBusClientBuilder();
        serviceBusClientBuilder.connectionString(String.format(CONNECTION_STRING_FORMAT, "test-namespace"));

        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.consumer.entity-name=test-topic",
                "spring.cloud.azure.servicebus.consumer.entity-type=topic"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(ServiceBusClientBuilder.class, () -> serviceBusClientBuilder)
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusConsumerClientConfiguration.class));
    }

    @Test
    void dedicatedConnectionInfoProvidedShouldConfigureDedicated() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.consumer.entity-name=test-queue",
                "spring.cloud.azure.servicebus.consumer.entity-type=queue",
                "spring.cloud.azure.servicebus.consumer.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusConsumerClientConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusConsumerClientConfiguration.NoneSessionConsumerClientConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusConsumerClientConfiguration.SessionConsumerClientConfiguration.class);
                assertThat(context).hasSingleBean(ServiceBusReceiverClient.class);
            });
    }

    @Test
    void sessionEnabledShouldConfigureSession() {
        ServiceBusClientBuilder serviceBusClientBuilder = new ServiceBusClientBuilder();
        serviceBusClientBuilder.connectionString(String.format(CONNECTION_STRING_FORMAT, "test-namespace"));

        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.entity-name=test-queue",
                "spring.cloud.azure.servicebus.entity-type=queue",
                "spring.cloud.azure.servicebus.consumer.session-enabled=true"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean(ServiceBusClientBuilder.class, () -> serviceBusClientBuilder)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusConsumerClientConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusConsumerClientConfiguration.SessionConsumerClientConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusConsumerClientConfiguration.NoneSessionConsumerClientConfiguration.class);
                assertThat(context).hasSingleBean(ServiceBusSessionReceiverClient.class);
                assertThat(context).doesNotHaveBean(ServiceBusReceiverClient.class);
            });
    }

    @Test
    void sessionEnabledWithDedicatedConnectionShouldConfigureSession() {
        contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.consumer.entity-name=test-queue",
                "spring.cloud.azure.servicebus.consumer.entity-type=queue",
                "spring.cloud.azure.servicebus.consumer.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace"),
                "spring.cloud.azure.servicebus.consumer.session-enabled=true"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusConsumerClientConfiguration.class);
                assertThat(context).hasSingleBean(AzureServiceBusConsumerClientConfiguration.SessionConsumerClientConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureServiceBusConsumerClientConfiguration.NoneSessionConsumerClientConfiguration.class);
                assertThat(context).hasSingleBean(ServiceBusSessionReceiverClient.class);
                assertThat(context).doesNotHaveBean(ServiceBusReceiverClient.class);
            });
    }

    @Test
    void customizerShouldBeCalledForNonSession() {
        ServiceBusReceiverClientBuilderCustomizer customizer = new ServiceBusReceiverClientBuilderCustomizer();
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.consumer.entity-name=test-queue",
                "spring.cloud.azure.servicebus.consumer.entity-type=queue",
                "spring.cloud.azure.servicebus.consumer.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean("customizer1", ServiceBusReceiverClientBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", ServiceBusReceiverClientBuilderCustomizer.class, () -> customizer)
            .run(context -> assertThat(customizer.getCustomizedTimes()).isEqualTo(2));
    }

    @Test
    void customizerShouldBeCalledForSession() {
        ServiceBusSessionReceiverClientBuilderCustomizer customizer = new ServiceBusSessionReceiverClientBuilderCustomizer();
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.consumer.entity-name=test-queue",
                "spring.cloud.azure.servicebus.consumer.entity-type=queue",
                "spring.cloud.azure.servicebus.consumer.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace"),
                "spring.cloud.azure.servicebus.consumer.session-enabled=true"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean("customizer1", ServiceBusSessionReceiverClientBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", ServiceBusSessionReceiverClientBuilderCustomizer.class, () -> customizer)
            .run(context -> assertThat(customizer.getCustomizedTimes()).isEqualTo(2));
    }

    @Test
    void otherCustomizerShouldNotBeCalled() {
        ServiceBusReceiverClientBuilderCustomizer customizer = new ServiceBusReceiverClientBuilderCustomizer();
        OtherBuilderCustomizer otherBuilderCustomizer = new OtherBuilderCustomizer();
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.servicebus.consumer.entity-name=test-queue",
                "spring.cloud.azure.servicebus.consumer.entity-type=queue",
                "spring.cloud.azure.servicebus.consumer.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .withBean("customizer1", ServiceBusReceiverClientBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", ServiceBusReceiverClientBuilderCustomizer.class, () -> customizer)
            .withBean("customizer3", OtherBuilderCustomizer.class, () -> otherBuilderCustomizer)
            .run(context -> {
                assertThat(customizer.getCustomizedTimes()).isEqualTo(2);
                assertThat(otherBuilderCustomizer.getCustomizedTimes()).isEqualTo(0);
            });
    }

    private static class ServiceBusReceiverClientBuilderCustomizer extends TestBuilderCustomizer<ServiceBusClientBuilder.ServiceBusReceiverClientBuilder> {

    }
    private static class ServiceBusSessionReceiverClientBuilderCustomizer extends TestBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder> {

    }

    private static class OtherBuilderCustomizer extends TestBuilderCustomizer<ConfigurationClientBuilder> {

    }

}
