// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.servicebus.jms.standard;

import com.azure.spring.sample.servicebus.jms.Receiver;
import com.azure.spring.sample.servicebus.jms.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;

@SpringBootTest
public class StandardServiceBusJmsIT {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Test
    void integrationTestQueue() throws InterruptedException {
        final String name = "Test Queue";
        jmsTemplate.convertAndSend(Receiver.QUEUE_NAME, new User(name));
        String msg = Receiver.EXCHANGER_QUEUE.exchange(name);
        Assertions.assertEquals(name, msg);
    }

    @Test
    void integrationTestTopic() throws InterruptedException {
        final String name = "Test Topic";
        jmsTemplate.convertAndSend(Receiver.TOPIC_NAME, new User(name));
        String msg = Receiver.EXCHANGER_TOPIC.exchange(name);
        Assertions.assertEquals(name, msg);
    }
}
