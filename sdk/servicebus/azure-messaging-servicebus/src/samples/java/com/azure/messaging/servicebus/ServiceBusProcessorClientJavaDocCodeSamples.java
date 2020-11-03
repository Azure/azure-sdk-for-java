// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import java.util.function.Consumer;

/**
 * Class contains sample code snippets that will be used in javadocs.
 */
public class ServiceBusProcessorClientJavaDocCodeSamples {

    /**
     * Creates a non session-enabled {@link ServiceBusProcessorClient}.
     */
    public void createServiceBusProcessorClient() {
        // BEGIN: com.azure.messaging.servicebus.servicebusprocessorclient.instantiation
        Consumer<ServiceBusProcessorMessageContext> messageProcessor = context -> {
            ServiceBusReceivedMessage message = context.getMessage();
            System.out.println("Received message " + message.getBody().toString());
        };

        Consumer<Throwable> errorHandler = throwable -> {
            System.out.println("Error when receiving messages " + throwable.getMessage());
            if (throwable instanceof ServiceBusReceiverException) {
                ServiceBusReceiverException serviceBusReceiverException = (ServiceBusReceiverException) throwable;
                System.out.println("Error source " + serviceBusReceiverException.getErrorSource());
            }
        };

        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
            .connectionString("<< connection-string >>")
            .processor()
            .queueName("<< queue name >>")
            .processMessage(messageProcessor)
            .processError(errorHandler)
            .buildProcessorClient();
        // END: com.azure.messaging.servicebus.servicebusprocessorclient.instantiation
    }

    /**
     * Creates a session-enabled {@link ServiceBusProcessorClient}.
     */
    public void createSessionEnabledServiceBusProcessorClient() {
        // BEGIN: com.azure.messaging.servicebus.servicebusprocessorclient.sessionclientinstantiation
        Consumer<ServiceBusProcessorMessageContext> messageProcessor = context -> {
            ServiceBusReceivedMessage message = context.getMessage();
            System.out.println("Received message " + message.getBody().toString()
                + " session: " + message.getSessionId());
        };

        Consumer<Throwable> errorHandler = throwable -> {
            System.out.println("Error when receiving messages " + throwable.getMessage());
            if (throwable instanceof ServiceBusReceiverException) {
                ServiceBusReceiverException serviceBusReceiverException = (ServiceBusReceiverException) throwable;
                System.out.println("Error source " + serviceBusReceiverException.getErrorSource());
            }
        };

        ServiceBusProcessorClient sessionProcessorClient = new ServiceBusClientBuilder()
            .connectionString("<< connection-string >>")
            .sessionProcessor()
            .queueName("<< session-enabled queue name >>")
            .maxConcurrentSessions(2)
            .processMessage(messageProcessor)
            .processError(errorHandler)
            .buildProcessorClient();
        // END: com.azure.messaging.servicebus.servicebusprocessorclient.sessionclientinstantiation
    }

    /**
     * Creates and starts non session-enabled {@link ServiceBusProcessorClient}.
     */
    public void createAndStartServiceBusProcessorClient() throws InterruptedException {

        // BEGIN: com.azure.messaging.servicebus.servicebusprocessorclient.start
        Consumer<ServiceBusProcessorMessageContext> messageProcessor = context -> {
            ServiceBusReceivedMessage message = context.getMessage();
            System.out.println("Received message " + message.getBody().toString());
        };

        Consumer<Throwable> errorHandler = throwable -> {
            System.out.println("Error when receiving messages " + throwable.getMessage());
            if (throwable instanceof ServiceBusReceiverException) {
                ServiceBusReceiverException serviceBusReceiverException = (ServiceBusReceiverException) throwable;
                System.out.println("Error source " + serviceBusReceiverException.getErrorSource());
            }
        };

        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
            .connectionString("<< connection-string >>")
            .processor()
            .queueName("<< queue name >>")
            .processMessage(messageProcessor)
            .processError(errorHandler)
            .buildProcessorClient();

        // Start the processor in the background
        processorClient.start();
        // END: com.azure.messaging.servicebus.servicebusprocessorclient.start
    }

    /**
     * Creates and starts session-enabled {@link ServiceBusProcessorClient}.
     */
    public void createAndStartSessionEnabledServiceBusProcessorClient() {
        // BEGIN: com.azure.messaging.servicebus.servicebusprocessorclient.startsession
        Consumer<ServiceBusProcessorMessageContext> messageProcessor = context -> {
            ServiceBusReceivedMessage message = context.getMessage();
            System.out.println("Received message " + message.getBody().toString()
                + " session: " + message.getSessionId());
        };

        Consumer<Throwable> errorHandler = throwable -> {
            System.out.println("Error when receiving messages " + throwable.getMessage());
            if (throwable instanceof ServiceBusReceiverException) {
                ServiceBusReceiverException serviceBusReceiverException = (ServiceBusReceiverException) throwable;
                System.out.println("Error source " + serviceBusReceiverException.getErrorSource());
            }
        };

        ServiceBusProcessorClient sessionProcessorClient = new ServiceBusClientBuilder()
            .connectionString("<< connection-string >>")
            .sessionProcessor()
            .queueName("<< session-enabled queue name >>")
            .maxConcurrentSessions(2)
            .processMessage(messageProcessor)
            .processError(errorHandler)
            .buildProcessorClient();

        // Start the processor in the background
        sessionProcessorClient.start();
        // END: com.azure.messaging.servicebus.servicebusprocessorclient.startsession
    }

}
