/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.servicebus;

import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.servicebus.SubscriptionClient;
import com.microsoft.azure.servicebus.TopicClient;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceBusAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ServiceBusAutoConfiguration.class));

    @Test
    public void returnNullIfSetConnectionStringOnly() {
        this.contextRunner.withPropertyValues(Constants.CONNECTION_STRING_PROPERTY,
                Constants.INVALID_CONNECTION_STRING);

        this.contextRunner.run((context) -> {
            assertThat(context).doesNotHaveBean(QueueClient.class);
            assertThat(context).doesNotHaveBean(TopicClient.class);
            assertThat(context).doesNotHaveBean(SubscriptionClient.class);
        });
    }

    @Test
    public void contextInitialisesWithInvalidConfigurationWhenNoBeansReferenced() {
        this.contextRunner.withPropertyValues(Constants.CONNECTION_STRING_PROPERTY, Constants.INVALID_CONNECTION_STRING)
                .withPropertyValues(Constants.QUEUE_NAME_PROPERTY, Constants.QUEUE_NAME)
                .withPropertyValues(Constants.QUEUE_RECEIVE_MODE_PROPERTY, Constants.QUEUE_RECEIVE_MODE.name())
                .withPropertyValues(Constants.TOPIC_NAME_PROPERTY, Constants.TOPIC_NAME)
                .withPropertyValues(Constants.SUBSCRIPTION_NAME_PROPERTY, Constants.SUBSCRIPTION_NAME)
                .withPropertyValues(Constants.SUBSCRIPTION_RECEIVE_MODE_PROPERTY,
                        Constants.SUBSCRIPTION_RECEIVE_MODE.name());

        this.contextRunner.run(context -> assertThat(context).isNotNull());
    }

    @Test
    public void cannotAutowireQueueClientWithInvalidConnectionString() {
        this.contextRunner.withPropertyValues(Constants.CONNECTION_STRING_PROPERTY, Constants.INVALID_CONNECTION_STRING)
                .withPropertyValues(Constants.QUEUE_NAME_PROPERTY, Constants.QUEUE_NAME)
                .withPropertyValues(Constants.QUEUE_RECEIVE_MODE_PROPERTY, Constants.QUEUE_RECEIVE_MODE.name());

        this.contextRunner.run(context -> assertThat(context).doesNotHaveBean(QueueClient.class));
    }

    @Test
    public void cannotAutowireTopicClientWithInvalidConnectionString() {
        this.contextRunner.withPropertyValues(Constants.CONNECTION_STRING_PROPERTY, Constants.INVALID_CONNECTION_STRING)
                .withPropertyValues(Constants.TOPIC_NAME_PROPERTY, Constants.TOPIC_NAME);

        this.contextRunner.run(context -> assertThat(context).doesNotHaveBean(TopicClient.class));
    }

    @Test
    public void cannotAutowireSubscriptionClientWithInvalidConnectionString() {
        this.contextRunner.withPropertyValues(Constants.CONNECTION_STRING_PROPERTY, Constants.INVALID_CONNECTION_STRING)
                .withPropertyValues(Constants.TOPIC_NAME_PROPERTY, Constants.TOPIC_NAME)
                .withPropertyValues(Constants.SUBSCRIPTION_NAME_PROPERTY, Constants.SUBSCRIPTION_NAME)
                .withPropertyValues(Constants.SUBSCRIPTION_RECEIVE_MODE_PROPERTY,
                        Constants.SUBSCRIPTION_RECEIVE_MODE.name());

        this.contextRunner.run(context -> assertThat(context).doesNotHaveBean(SubscriptionClient.class));
    }

    @Test
    public void cannotAutowireSubscriptionClientWithInvalidCredential() {
        this.contextRunner.withPropertyValues(Constants.CONNECTION_STRING_PROPERTY, Constants.CONNECTION_STRING)
                .withPropertyValues(Constants.TOPIC_NAME_PROPERTY, Constants.TOPIC_NAME)
                .withPropertyValues(Constants.SUBSCRIPTION_NAME_PROPERTY, Constants.SUBSCRIPTION_NAME)
                .withPropertyValues(Constants.SUBSCRIPTION_RECEIVE_MODE_PROPERTY,
                        Constants.SUBSCRIPTION_RECEIVE_MODE.name());

        this.contextRunner.run(context -> assertThat(context).doesNotHaveBean(SubscriptionClient.class));
    }
}
