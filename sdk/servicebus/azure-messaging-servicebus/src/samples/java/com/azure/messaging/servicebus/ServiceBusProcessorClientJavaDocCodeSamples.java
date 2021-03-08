// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

/**
 * Class contains sample code snippets that will be used in javadocs.
 *
 * @see ServiceBusProcessorClient
 */
public class ServiceBusProcessorClientJavaDocCodeSamples {
    String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
    String queueName = System.getenv("AZURE_SERVICEBUS_SAMPLE_SESSION_QUEUE_NAME");

    /**
     * Creates a non session-enabled {@link ServiceBusProcessorClient}.
     */
    @Test
    public void createServiceBusProcessorClient() {
        // BEGIN: com.azure.messaging.servicebus.servicebusprocessorclient#instantiation
        Consumer<ServiceBusReceivedMessageContext> onMessage = context -> {
            ServiceBusReceivedMessage message = context.getMessage();
            System.out.printf("Processing message. Sequence #: %s. Contents: %s%n",
                message.getSequenceNumber(), message.getBody());
        };

        Consumer<ServiceBusErrorContext> onError = context -> {
            System.out.printf("Error when receiving messages from namespace: '%s'. Entity: '%s'%n",
                context.getFullyQualifiedNamespace(), context.getEntityPath());

            if (context.getException() instanceof ServiceBusException) {
                ServiceBusException exception = (ServiceBusException) context.getException();
                System.out.printf("Error source: %s, reason %s%n", context.getErrorSource(),
                    exception.getReason());
            } else {
                System.out.printf("Error occurred: %s%n", context.getException());
            }
        };

        // Retrieve 'connectionString/queueName' from your configuration.

        ServiceBusProcessorClient processor = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .processor()
            .queueName(queueName)
            .processMessage(onMessage)
            .processError(onError)
            .buildProcessorClient();

        // Start the processor in the background
        processor.start();
        // END: com.azure.messaging.servicebus.servicebusprocessorclient#instantiation
    }

    /**
     * Creates a session-enabled {@link ServiceBusProcessorClient}.
     */
    @Test
    public void createSessionEnabledServiceBusProcessorClient() {
        // BEGIN: com.azure.messaging.servicebus.servicebusprocessorclient#session-instantiation
        Consumer<ServiceBusReceivedMessageContext> onMessage = context -> {
            ServiceBusReceivedMessage message = context.getMessage();
            System.out.printf("Processing message. Session: %s, Sequence #: %s. Contents: %s%n",
                message.getSessionId(), message.getSequenceNumber(), message.getBody());
        };

        Consumer<ServiceBusErrorContext> onError = context -> {
            System.out.printf("Error when receiving messages from namespace: '%s'. Entity: '%s'%n",
                context.getFullyQualifiedNamespace(), context.getEntityPath());

            if (context.getException() instanceof ServiceBusException) {
                ServiceBusException exception = (ServiceBusException) context.getException();
                System.out.printf("Error source: %s, reason %s%n", context.getErrorSource(),
                    exception.getReason());
            } else {
                System.out.printf("Error occurred: %s%n", context.getException());
            }
        };

        // Retrieve 'connectionString/queueName' from your configuration.

        ServiceBusProcessorClient sessionProcessor = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sessionProcessor()
            .queueName(queueName)
            .maxConcurrentSessions(2)
            .processMessage(onMessage)
            .processError(onError)
            .buildProcessorClient();

        // Start the processor in the background
        sessionProcessor.start();
        // END: com.azure.messaging.servicebus.servicebusprocessorclient#session-instantiation
    }
}
