// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.integration.tests.servicebus;

import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.*;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusErrorHandler;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusRecordMessageListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = ServiceBusIT.TestConfig.class)
@ActiveProfiles("servicebus")
public class ServiceBusIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusIT.class);
    private final String data = "service bus test";

    @Autowired
    private ServiceBusSenderClient senderClient;

    @Autowired
    private ServiceBusReceiverClient receiverClient;

    @Autowired
    private ServiceBusProcessorClient processorClient;

    @EnableAutoConfiguration
    static class TestConfig {
        @Bean
        ServiceBusRecordMessageListener messageListener() {
            return message -> {
            };
        }

        @Bean
        ServiceBusErrorHandler errorHandler() {
            return errorContext -> {
            };
        }
    }

    @Test
    public void testServiceBusOperation() {
        LOGGER.info("ServiceBusIT begin.");
        senderClient.sendMessage(new ServiceBusMessage(data));
        senderClient.close();
        IterableStream<ServiceBusReceivedMessage> receivedMessages = receiverClient.receiveMessages(1);
        if (receivedMessages.stream().iterator().hasNext()) {
            ServiceBusReceivedMessage message = receivedMessages.stream().iterator().next();
            Assertions.assertEquals(data, message.getBody().toString());
        }
        processorClient.start();
        Assertions.assertTrue(processorClient.isRunning());
        //TODO
        processorClient.close();
        Assertions.assertFalse(processorClient.isRunning());
        LOGGER.info("ServiceBusIT end.");
    }
}
