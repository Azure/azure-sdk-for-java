package com.azure.spring.cloud.integration.tests.servicebus;

import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("service-bus")
public class ServiceBusIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusIT.class);
    private final String data = "service bus test";

    @Autowired
    private ServiceBusSenderClient senderClient;

    @Autowired
    private ServiceBusReceiverClient receiverClient;

    @Test
    public void testServiceBusOperation() {
        LOGGER.info("ServiceBusIT begin.");
        senderClient.sendMessage(new ServiceBusMessage(data));
        senderClient.close();
        IterableStream<ServiceBusReceivedMessage> receivedMessages = receiverClient.receiveMessages(1);
        if (receivedMessages.stream().iterator().hasNext()) {
            ServiceBusReceivedMessage message = receivedMessages.stream().iterator().next();
            Assertions.assertEquals(data, message.getBody());
        }
        LOGGER.info("ServiceBusIT end.");
    }
}
