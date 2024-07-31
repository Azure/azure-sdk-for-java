// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.integration.tests.servicebus;

import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusErrorHandler;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusRecordMessageListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        //exclude these two configurations to avoid activating the Spring Cloud Stream Binder, which will be
        // activated unintentionally by error handler (which is a Consumer<> bean)
        "spring.autoconfigure.exclude=org.springframework.cloud.stream.config.BindingServiceConfiguration"
            + ",org.springframework.cloud.stream.function.FunctionConfiguration"
    })
@ActiveProfiles("servicebus")
public class ServiceBusIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusIT.class);
    private static final String DATA1 = "service bus test1";
    private static final String DATA2 = "service bus test2";
    private static CountDownLatch LATCH = new CountDownLatch(1);
    private static String MESSAGE = "";

    @Autowired
    private ServiceBusSenderClient senderClient;

    @Autowired
    private ServiceBusReceiverClient receiverClient;

    @Autowired
    private ServiceBusProcessorClient processorClient;

    @TestConfiguration
    static class TestConfig {
        @Bean
        ServiceBusRecordMessageListener messageListener() {
            return message -> {
                MESSAGE = message.getMessage().getBody().toString();
                LATCH.countDown();
            };
        }

        @Bean
        ServiceBusErrorHandler errorHandler() {
            return errorContext -> {
            };
        }
    }

    @Test
    @Timeout(120)
    public void testServiceBusOperation() throws InterruptedException {
        LOGGER.info("ServiceBusIT begin.");
        // Wait for Service Bus initialization to complete
        Thread.sleep(20000);
        senderClient.sendMessage(new ServiceBusMessage(DATA1));
        IterableStream<ServiceBusReceivedMessage> receivedMessages = receiverClient.receiveMessages(1);
        Assertions.assertEquals(1, receivedMessages.stream().count());
        if (receivedMessages.stream().iterator().hasNext()) {
            ServiceBusReceivedMessage message = receivedMessages.stream().iterator().next();
            Assertions.assertEquals(DATA1, message.getBody().toString());
            receiverClient.complete(message);
        }
        senderClient.sendMessage(new ServiceBusMessage(DATA2));
        senderClient.close();
        processorClient.start();
        Assertions.assertTrue(processorClient.isRunning());
        LATCH.await(15, TimeUnit.SECONDS);
        Assertions.assertEquals(DATA2, MESSAGE);
        processorClient.close();
        Assertions.assertFalse(processorClient.isRunning());
        LOGGER.info("ServiceBusIT end.");
    }
}
