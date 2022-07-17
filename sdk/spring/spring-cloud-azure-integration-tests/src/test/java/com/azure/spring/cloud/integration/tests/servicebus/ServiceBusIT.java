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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = ServiceBusIT.TestConfig.class)
@ActiveProfiles(value = {"service-bus", "service-bus-jms"})
public class ServiceBusIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusIT.class);
    private final String data = "service bus test";
    private static CountDownLatch countDownLatch = new CountDownLatch(1);

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
            return new ServiceBusRecordMessageListener() {
                @Override
                public void onMessage(ServiceBusReceivedMessageContext message) {
                    countDownLatch.countDown();
                }
            };
        };
        @Bean
        ServiceBusErrorHandler errorHandler() {
            return new ServiceBusErrorHandler() {
                @Override
                public void accept(ServiceBusErrorContext context) { }
            };
        }
    }

    @Test
    public void testServiceBusOperation() throws InterruptedException {
        LOGGER.info("ServiceBusIT begin.");
        senderClient.sendMessage(new ServiceBusMessage(data));
        senderClient.close();
        IterableStream<ServiceBusReceivedMessage> receivedMessages = receiverClient.receiveMessages(1);
        if (receivedMessages.stream().iterator().hasNext()) {
            ServiceBusReceivedMessage message = receivedMessages.stream().iterator().next();
            Assertions.assertEquals(data, message.getBody().toString());
        }
        processorClient.start();
        boolean success = countDownLatch.await(5, TimeUnit.SECONDS);
        processorClient.close();
        Assertions.assertTrue(success);
        LOGGER.info("ServiceBusIT end.");
    }
}
