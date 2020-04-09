package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ReceiveMode;

import java.util.concurrent.atomic.AtomicBoolean;

public class ServiceBusMultiSessionProcessorSample {

    public static void main(String[] args) throws Exception {

        ServiceBusMultiSessionProcessorClient multiSessionProcessorClient = new ServiceBusClientBuilder()
            .connectionString("connectionString")
            .receiverMultiSession()
            .receiveMode(ReceiveMode.PEEK_LOCK)
            .queueName("<<queue-name>>")
            .buildMultiSessionProcessorClient();

        System.out.println("Starting message processor");
        final AtomicBoolean isRunning = new AtomicBoolean(true);
        multiSessionProcessorClient.start();

        System.out.println("Stopping message processor");
        multiSessionProcessorClient.stop();

        System.out.println("Exiting process");
    }
}
