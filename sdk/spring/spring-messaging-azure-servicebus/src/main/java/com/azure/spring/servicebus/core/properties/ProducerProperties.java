// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.properties;

import com.azure.spring.service.servicebus.properties.ServiceBusProducerDescriptor;

public class ProducerProperties extends CommonProperties implements ServiceBusProducerDescriptor {

    private String queueName;
    private String topicName;

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

}
