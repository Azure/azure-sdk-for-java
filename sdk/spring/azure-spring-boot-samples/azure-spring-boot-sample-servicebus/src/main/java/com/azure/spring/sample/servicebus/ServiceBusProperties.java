// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.servicebus;

import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Validated
@ConfigurationProperties("azure.servicebus")
public class ServiceBusProperties {
    /**
     * Service Bus connection string.
     */
    @NotEmpty
    private String connectionString;

    /**
     * Queue name. Entity path of the queue.
     */
    private String queueName;

    /**
     * Queue receive mode.
     */
    private ServiceBusReceiveMode queueReceiveMode;

    /**
     * Topic name. Entity path of the topic.
     */
    private String topicName;

    /**
     * Subscription name.
     */
    private String subscriptionName;

    /**
     * Subscription receive mode.
     */
    private ServiceBusReceiveMode subscriptionReceiveMode;

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public ServiceBusReceiveMode getQueueReceiveMode() {
        return queueReceiveMode;
    }

    public void setQueueReceiveMode(ServiceBusReceiveMode queueReceiveMode) {
        this.queueReceiveMode = queueReceiveMode;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public void setSubscriptionName(String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }

    public ServiceBusReceiveMode getSubscriptionReceiveMode() {
        return subscriptionReceiveMode;
    }

    public void setSubscriptionReceiveMode(ServiceBusReceiveMode subscriptionReceiveMode) {
        this.subscriptionReceiveMode = subscriptionReceiveMode;
    }
}

