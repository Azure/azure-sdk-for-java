// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.servicebus.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.Exchanger;

@Component
public class Receiver {

    public static final String QUEUE_NAME = "que001";
    public static final String TOPIC_NAME = "topic001";
    public static final String SUBSCRIPTION_NAME = "sub001";

    private final Logger logger = LoggerFactory.getLogger(Receiver.class);

    public static final Exchanger<String> EXCHANGER_QUEUE = new Exchanger<>();
    public static final Exchanger<String> EXCHANGER_TOPIC = new Exchanger<>();


    @JmsListener(destination = QUEUE_NAME, containerFactory = "jmsListenerContainerFactory")
    public void receiveQueueMessage(User user) throws InterruptedException {
        logger.info("Received message from queue: {}", user.getName());
        EXCHANGER_QUEUE.exchange(user.getName());
    }

    @JmsListener(destination = TOPIC_NAME, containerFactory = "topicJmsListenerContainerFactory",
        subscription = SUBSCRIPTION_NAME)
    public void receiveTopicMessage(User user) throws InterruptedException{
        logger.info("Received message from topic: {}", user.getName());
        EXCHANGER_TOPIC.exchange(user.getName());
    }

}
