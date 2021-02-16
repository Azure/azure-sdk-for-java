// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import java.util.concurrent.TimeUnit;

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
        // Create an instance of session-enabled processor through the ServiceBusClientBuilder that processes
        // two sessions concurrently.
        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
            .connectionString("<< connection-string ")
            .sessionProcessor()
            .queueName("<< session-enabled queue name >>")
            .maxConcurrentSessions(2)
            .processMessage(ServiceBusSessionProcessorSample::processMessage)
            .processError(ServiceBusSessionProcessorSample::processError)
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

    /**
     * Processes each message from the Service Bus entity.
     *
     * @param context Received message context.
     */
    private static void processMessage(ServiceBusReceivedMessageContext context) {
        ServiceBusReceivedMessage message = context.getMessage();
        System.out.printf("Processing message. Session: %s, Sequence #: %s. Contents: %s%n", message.getMessageId(),
            message.getSequenceNumber(), message.getBody());

        // When this message function completes, the message is automatically completed. If an exception is
        // thrown in here, the message is abandoned.
        // To disable this behaviour, toggle ServiceBusSessionProcessorClientBuilder.disableAutoComplete()
        // when building the session receiver.
    }

    /**
     * Processes an exception that occurred in the Service Bus Processor.
     *
     * @param context Context around the exception that occurred.
     */
    private static void processError(ServiceBusErrorContext context) {
        System.out.printf("Error when receiving messages from namespace: '%s'. Entity: '%s'%n",
            context.getFullyQualifiedNamespace(), context.getEntityPath());

        if (!(context.getException() instanceof ServiceBusException)) {
            System.out.printf("Non-ServiceBusException occurred: %s%n", context.getException());
            return;
        }

        ServiceBusException exception = (ServiceBusException) context.getException();
        System.out.printf("ServiceBusException source: %s. Reason: %s. Is transient? %s%n", context.getErrorSource(),
            exception.getReason(), exception.isTransient());
    }
}
