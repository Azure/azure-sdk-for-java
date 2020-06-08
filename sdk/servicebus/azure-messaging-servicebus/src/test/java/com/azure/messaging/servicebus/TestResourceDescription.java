// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

class TestResourceDescription {
    private final String queueName;
    private final String topicName;
    private final String subscriberName;

    TestResourceDescription(String queueName, String topicName, String subscriberName) {
        this.queueName = queueName;
        this.topicName = topicName;
        this.subscriberName = subscriberName;
    }

    public String getQueueName() {
        return queueName;
    }

    public String getTopicName() {
        return topicName;
    }

    public String getSubscriberName() {
        return subscriberName;
    }
}
