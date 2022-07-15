package com.azure.spring.cloud.integration.tests.servicebus.jms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("service-bus-jms")
public class ServiceBusJmsIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusJmsIT.class);
    private final String data = "service bus jms test";

    @Autowired
    private JmsTemplate jmsTemplate;

    @Test
    public void testServiceBusJmsOperation() throws InterruptedException {
        LOGGER.info("ServiceBusJmsIT begin.");
        jmsTemplate.convertAndSend(ServiceBusJmsReceiver.QUEUE_NAME, data);
        LOGGER.info("Send message: {}",data);
        String msg = ServiceBusJmsReceiver.EXCHANGER.exchange(null);
        Assertions.assertEquals(msg, data);
        LOGGER.info("ServiceBusJmsIT end.");
    }

}
