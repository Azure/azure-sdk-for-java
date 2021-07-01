// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.messaging.servicebus;

import com.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;
import com.azure.spring.messaging.annotation.AzureMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TopicController {

    private static final String TOPIC_NAME = "topic";
    private static final String SUBSCRIPTION_NAME = "sub";

    @Autowired
    ServiceBusTopicOperation topicOperation;

    @PostMapping("/messages")
    public String send(@RequestParam("message") String message) {
        this.topicOperation.sendAsync(TOPIC_NAME, MessageBuilder.withPayload(message).build());
        return message;
    }

    @AzureMessageListener(destination = TOPIC_NAME,group = SUBSCRIPTION_NAME)
    public void handleMessage(String message) {
        System.out.println(String.format("New service bus topic message received: '%s'", message));
    }

}
