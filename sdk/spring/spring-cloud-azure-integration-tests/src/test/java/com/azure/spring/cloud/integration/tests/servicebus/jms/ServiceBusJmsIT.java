// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.integration.tests.servicebus.jms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.Exchanger;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("servicebus-jms")
public class ServiceBusJmsIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusJmsIT.class);
    private static final String DATA = "service bus jms test";
    private static final String QUEUE_NAME = "que001";
    private final Exchanger<String> EXCHANGER = new Exchanger<>();

    @Autowired
    private JmsTemplate jmsTemplate;

    @Test
    @Timeout(70)
    public void testServiceBusJmsOperation() throws InterruptedException {
        LOGGER.info("ServiceBusJmsIT begin.");
        jmsTemplate.convertAndSend(QUEUE_NAME, DATA);
        LOGGER.info("Send message: {}", DATA);
        String msg = EXCHANGER.exchange(null);
        Assertions.assertEquals(DATA, msg);
        LOGGER.info("ServiceBusJmsIT end.");
    }

    @JmsListener(destination = QUEUE_NAME, containerFactory = "jmsListenerContainerFactory")
    public void receiveQueueMessage(String message) throws InterruptedException {
        LOGGER.info("Received message from queue: {}", message);
        EXCHANGER.exchange(message);
    }
}
