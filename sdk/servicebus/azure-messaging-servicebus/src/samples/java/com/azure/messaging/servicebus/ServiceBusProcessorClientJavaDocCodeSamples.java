// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
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
     * Creates a non session-enabled {@link ServiceBusProcessorClient} to receive in PeekLock mode.
     */
    @Test
    public void createServiceBusProcessorClientInPeekLockMode() {
        // BEGIN: com.azure.messaging.servicebus.servicebusprocessorclient#receive-mode-peek-lock-instantiation
        Consumer<ServiceBusReceivedMessageContext> processMessage = context -> {
            final ServiceBusReceivedMessage message = context.getMessage();
            // Randomly complete or abandon each message. Ideally, in real-world scenarios, if the business logic
            // handling message reaches desired state such that it doesn't require Service Bus to redeliver
            // the same message, then context.complete() should be called otherwise context.abandon().
            final boolean success = Math.random() < 0.5;
            if (success) {
                try {
                    context.complete();
                } catch (Exception completionError) {
                    System.out.printf("Completion of the message %s failed\n", message.getMessageId());
                    completionError.printStackTrace();
                }
            } else {
                try {
                    context.abandon();
                } catch (Exception abandonError) {
                    System.out.printf("Abandoning of the message %s failed\n", message.getMessageId());
                    abandonError.printStackTrace();
                }
            }
        };

        // Sample code that gets called if there's an error
        Consumer<ServiceBusErrorContext> processError = errorContext -> {
            System.err.println("Error occurred while receiving message: " + errorContext.getException());
        };

        // create the processor client via the builder and its sub-builder
        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
            .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
            .processor()
            .queueName("<< QUEUE NAME >>")
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .disableAutoComplete()  // Make sure to explicitly opt in to manual settlement (e.g. complete, abandon).
            .processMessage(processMessage)
            .processError(processError)
            .disableAutoComplete()
            .buildProcessorClient();

        // Starts the processor in the background and returns immediately
        processorClient.start();
        // END: com.azure.messaging.servicebus.servicebusprocessorclient#receive-mode-peek-lock-instantiation
    }

    /**
     * Creates a non session-enabled {@link ServiceBusProcessorClient} to receive in PeekLock mode.
     */
    @Test
    public void createServiceBusProcessorClientInReceiveAndDeleteMode() {
        // BEGIN: com.azure.messaging.servicebus.servicebusprocessorclient#receive-mode-receive-and-delete-instantiation
        Consumer<ServiceBusReceivedMessageContext> processMessage = context -> {
            final ServiceBusReceivedMessage message = context.getMessage();
            System.out.printf("Processing message. Session: %s, Sequence #: %s. Contents: %s%n",
                message.getSessionId(), message.getSequenceNumber(), message.getBody());
        };

        // Sample code that gets called if there's an error
        Consumer<ServiceBusErrorContext> processError = errorContext -> {
            System.err.println("Error occurred while receiving message: " + errorContext.getException());
        };

        // create the processor client via the builder and its sub-builder
        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
            .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
            .processor()
            .queueName("<< QUEUE NAME >>")
            .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
            .processMessage(processMessage)
            .processError(processError)
            .disableAutoComplete()
            .buildProcessorClient();

        // Starts the processor in the background and returns immediately
        processorClient.start();
        // END: com.azure.messaging.servicebus.servicebusprocessorclient#receive-mode-receive-and-delete-instantiation
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
