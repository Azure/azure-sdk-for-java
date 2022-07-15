package com.azure.spring.cloud.integration.tests.servicebus.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.Exchanger;

@Component
public class ServiceBusJmsReceiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusJmsReceiver.class);
    public static final String QUEUE_NAME = "que001";
    public static final Exchanger<String> EXCHANGER = new Exchanger<>();

    @JmsListener(destination = QUEUE_NAME, containerFactory = "jmsListenerContainerFactory")
    public void receiveQueueMessage(String message) throws InterruptedException {
        LOGGER.info("Received message from queue: {}", message);
        EXCHANGER.exchange(message);
    }

}
