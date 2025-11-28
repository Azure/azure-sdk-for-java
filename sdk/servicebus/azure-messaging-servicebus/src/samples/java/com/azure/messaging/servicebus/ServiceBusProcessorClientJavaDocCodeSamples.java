// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

/**
 * Class contains sample code snippets that will be used in javadocs.
 *
 * @see ServiceBusProcessorClient
 */
public class ServiceBusProcessorClientJavaDocCodeSamples {
    /**
     * Fully qualified namespace is the host name of the Service Bus resource.  It can be found by navigating to the
     * Service Bus namespace and looking in the "Essentials" panel.
     */
    private final String fullyQualifiedNamespace = System.getenv("AZURE_SERVICEBUS_FULLY_QUALIFIED_DOMAIN_NAME");
    /**
     * Name of a queue inside the Service Bus namespace.
     */
    private final String queueName = System.getenv("AZURE_SERVICEBUS_SAMPLE_QUEUE_NAME");
    /**
     * Name of a session-enabled queue in the Service Bus namespace.
     */
    private final String sessionEnabledQueueName = System.getenv("AZURE_SERVICEBUS_SAMPLE_SESSION_QUEUE_NAME");

    /**
     * Creates a non session-enabled {@link ServiceBusProcessorClient} to receive in PeekLock mode.
     */
    @Test
    public void createServiceBusProcessorClientInPeekLockMode() {
        // BEGIN: com.azure.messaging.servicebus.servicebusprocessorclient#receive-mode-peek-lock-instantiation
        // Function that gets called whenever a message is received.
        Consumer<ServiceBusReceivedMessageContext> processMessage = context -> {
            final ServiceBusReceivedMessage message = context.getMessage();
            // Randomly complete or abandon each message. Ideally, in real-world scenarios, if the business logic
            // handling message reaches desired state such that it doesn't require Service Bus to redeliver
            // the same message, then context.complete() should be called otherwise context.abandon().
            final boolean success = Math.random() < 0.5;
            if (success) {
                try {
                    context.complete();
                } catch (RuntimeException error) {
                    System.out.printf("Completion of the message %s failed.%n Error: %s%n",
                        message.getMessageId(), error);
                }
            } else {
                try {
                    context.abandon();
                } catch (RuntimeException error) {
                    System.out.printf("Abandoning of the message %s failed.%nError: %s%n",
                        message.getMessageId(), error);
                }
            }
        };

        // Sample code that gets called if there's an error
        Consumer<ServiceBusErrorContext> processError = errorContext -> {
            if (errorContext.getException() instanceof ServiceBusException) {
                ServiceBusException exception = (ServiceBusException) errorContext.getException();

                System.out.printf("Error source: %s, reason %s%n", errorContext.getErrorSource(),
                    exception.getReason());
            } else {
                System.out.printf("Error occurred: %s%n", errorContext.getException());
            }
        };

        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Create the processor client via the builder and its sub-builder
        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, tokenCredential)
            .processor()
            .queueName(queueName)
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .disableAutoComplete()  // Make sure to explicitly opt in to manual settlement (e.g. complete, abandon).
            .processMessage(processMessage)
            .processError(processError)
            .buildProcessorClient();

        // Starts the processor in the background. Control returns immediately.
        processorClient.start();

        // Stop processor and dispose when done processing messages.
        processorClient.stop();
        processorClient.close();
        // END: com.azure.messaging.servicebus.servicebusprocessorclient#receive-mode-peek-lock-instantiation
    }

    /**
     * Creates a non session-enabled {@link ServiceBusProcessorClient} to receive in PeekLock mode.
     */
    @Test
    public void createServiceBusProcessorClientInReceiveAndDeleteMode() {
        // BEGIN: com.azure.messaging.servicebus.servicebusprocessorclient#receive-mode-receive-and-delete-instantiation
        // Function that gets called whenever a message is received.
        Consumer<ServiceBusReceivedMessageContext> processMessage = context -> {
            final ServiceBusReceivedMessage message = context.getMessage();
            System.out.printf("Processing message. Session: %s, Sequence #: %s. Contents: %s%n",
                message.getSessionId(), message.getSequenceNumber(), message.getBody());
        };

        // Sample code that gets called if there's an error
        Consumer<ServiceBusErrorContext> processError = errorContext -> {
            if (errorContext.getException() instanceof ServiceBusException) {
                ServiceBusException exception = (ServiceBusException) errorContext.getException();

                System.out.printf("Error source: %s, reason %s%n", errorContext.getErrorSource(),
                    exception.getReason());
            } else {
                System.out.printf("Error occurred: %s%n", errorContext.getException());
            }
        };

        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Create the processor client via the builder and its sub-builder
        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, tokenCredential)
            .processor()
            .queueName(queueName)
            .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
            .processMessage(processMessage)
            .processError(processError)
            .buildProcessorClient();

        // Starts the processor in the background. Control returns immediately.
        processorClient.start();

        // Stop processor and dispose when done processing messages.
        processorClient.stop();
        processorClient.close();
        // END: com.azure.messaging.servicebus.servicebusprocessorclient#receive-mode-receive-and-delete-instantiation
    }

    /**
     * Creates a session-enabled {@link ServiceBusProcessorClient}.
     */
    @Test
    public void createSessionEnabledServiceBusProcessorClient() {
        // BEGIN: com.azure.messaging.servicebus.servicebusprocessorclient#session-instantiation
        // Function that gets called whenever a message is received.
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

        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Create the processor client via the builder and its sub-builder
        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        ServiceBusProcessorClient sessionProcessor = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, tokenCredential)
            .sessionProcessor()
            .queueName(sessionEnabledQueueName)
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .disableAutoComplete()
            .maxConcurrentSessions(2)
            .processMessage(onMessage)
            .processError(onError)
            .buildProcessorClient();

        // Starts the processor in the background. Control returns immediately.
        sessionProcessor.start();

        // Stop processor and dispose when done processing messages.
        sessionProcessor.stop();
        sessionProcessor.close();
        // END: com.azure.messaging.servicebus.servicebusprocessorclient#session-instantiation
    }
}
