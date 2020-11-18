// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Sample to demonstrate the creation of a {@link ServiceBusProcessorClient} and starting the processor to receive
 * messages.
 */
public class ServiceBusProcessorSample {

    /**
     * Main method to start the sample application.
     * @param args Ignored args.
     * @throws InterruptedException If the application is interrupted.
     */
    public static void main(String[] args) throws InterruptedException {
        // Consumer that processes a single message received from Service Bus
        Consumer<ServiceBusReceivedMessageContext> messageProcessor = context -> {
            ServiceBusReceivedMessage message = context.getMessage();
            System.out.println("Received message " + message.getBody().toString());
        };

        final CountDownLatch countdownLatch = new CountDownLatch(1);

        // Consumer that handles any errors that occur when receiving messages
        Consumer<ServiceBusErrorContext> errorHandler = errorContext -> {
            if (errorContext.getException() instanceof ServiceBusException) {
                final ServiceBusException serviceBusException = (ServiceBusException) errorContext.getException();
                final ServiceBusFailureReason reason = serviceBusException.getReason();

                if (reason == ServiceBusFailureReason.MESSAGING_ENTITY_DISABLED
                    || reason == ServiceBusFailureReason.MESSAGING_ENTITY_NOT_FOUND
                    || reason == ServiceBusFailureReason.UNAUTHORIZED) {
                    System.out.printf("An unrecoverable error occurred. Stopping processing with reason %s: %s\n",
                        reason, serviceBusException.toString());
                    countdownLatch.countDown();
                } else if (reason == ServiceBusFailureReason.MESSAGE_LOCK_LOST) {
                    System.out.printf("Message lock lost for message: %s", errorContext.getException().toString());
                } else if (reason == ServiceBusFailureReason.SERVICE_BUSY) {
                    try {
                        // choosing an arbitrary amount of time to wait.
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.printf("Error source %s, reason %s, message: %s\n", serviceBusException.getErrorSource(),
                        reason, errorContext.getException().getMessage());
                }
            } else {
                System.out.printf("Exception: %s\n", errorContext.getException().toString());
            }
        };

        // Create an instance of the processor through the ServiceBusClientBuilder
        final ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
            .connectionString("<< connection-string >>")
            .processor()
            .queueName("<< queue name >>")
            .processMessage(messageProcessor)
            .processError(errorHandler)
            .buildProcessorClient();

        System.out.println("Starting the processor");
        processorClient.start();

        System.out.println("Listening for 10 seconds...");
        if (countdownLatch.await(10, TimeUnit.SECONDS)) {
            System.out.println("Closing processor due to fatal error");
        } else {
            System.out.println("Closing processor");
        }

        processorClient.close();
    }
}
