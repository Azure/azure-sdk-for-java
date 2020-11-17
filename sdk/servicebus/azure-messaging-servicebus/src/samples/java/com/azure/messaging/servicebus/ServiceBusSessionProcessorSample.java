// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Sample to demonstrate the creation of a session-enabled {@link ServiceBusProcessorClient} and starting the processor
 * to receive messages.
 */
public class ServiceBusSessionProcessorSample {

    /**
     * Main method to start the sample application.
     * @param args Ignored args.
     * @throws InterruptedException If the application is interrupted.
     */
    public static void main(String[] args) throws InterruptedException {
        // Consumer that processes a single message received from Service Bus
        Consumer<ServiceBusReceivedMessageContext> messageProcessor = context -> {
            ServiceBusReceivedMessage message = context.getMessage();
            System.out.println("Received message " + message.getBody().toString()
                + " session: " + message.getSessionId());
        };

        // Consumer that handles any errors that occur when receiving messages
        Consumer<ServiceBusErrorContext> errorHandler = errorContext -> {
            System.out.println("Error when receiving messages " + errorContext.getException().getMessage());
            if (errorContext.getException() instanceof ServiceBusException) {
                ServiceBusException serviceBusException = (ServiceBusException) errorContext.getException();
                System.out.printf("Error source %s, reason %s\n", serviceBusException.getErrorSource(),
                    serviceBusException.getReason());
            }
        };

        // Create an instance of session-enabled processor through the ServiceBusClientBuilder that processes
        // two sessions concurrently.
        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
            .connectionString("<< connection-string ")
            .sessionProcessor()
            .queueName("<< session-enabled queue name >>")
            .maxConcurrentSessions(2)
            .processMessage(messageProcessor)
            .processError(errorHandler)
            .buildProcessorClient();

        System.out.println("Starting the processor");
        processorClient.start();

        TimeUnit.SECONDS.sleep(10);
        System.out.println("Stopping the processor");
        processorClient.stop();

        TimeUnit.SECONDS.sleep(10);
        System.out.println("Resuming the processor");
        processorClient.start();

        TimeUnit.SECONDS.sleep(10);
        System.out.println("Closing the processor");
        processorClient.close();
    }
}
