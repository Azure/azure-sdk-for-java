// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

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

        // Consumer that handles any errors that occur when receiving messages
        Consumer<ServiceBusProcessErrorContext> errorHandler = serviceBusProcessErrorContext -> {
            System.out.println("Error when receiving messages " + serviceBusProcessErrorContext.getException().getMessage());
            if (serviceBusProcessErrorContext.getException() instanceof ServiceBusException) {
                ServiceBusException serviceBusException = (ServiceBusException) serviceBusProcessErrorContext.getException();
                System.out.println("Error source " + serviceBusException.getErrorSource() + ", reason " + serviceBusException.getReason());
            }
        };

        // Create an instance of the processor through the ServiceBusClientBuilder
        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
            .connectionString("<< connection-string >>")
            .processor()
            .queueName("<< queue name >>")
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
