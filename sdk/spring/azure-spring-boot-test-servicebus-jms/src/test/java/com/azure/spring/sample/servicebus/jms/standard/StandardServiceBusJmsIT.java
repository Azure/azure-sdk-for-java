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
    void integrationTest() throws InterruptedException {

        final String name = "Tester";
        jmsTemplate.convertAndSend(Receiver.QUEUE_NAME, new User(name));
        String msg = Receiver.EXCHANGER.exchange(name);
        Assertions.assertEquals(name, msg);
    }
}
