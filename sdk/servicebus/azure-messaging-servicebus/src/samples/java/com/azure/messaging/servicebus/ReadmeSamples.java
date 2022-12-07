// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.IterableStream;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.messaging.servicebus.models.SubQueue;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {
    /**
     * Code sample for creating an asynchronous Service Bus sender.
     */
    public void createAsynchronousServiceBusSender() {
        // BEGIN: readme-sample-createAsynchronousServiceBusSender
        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
            .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
            .sender()
            .queueName("<< QUEUE NAME >>")
            .buildClient();
        // END: readme-sample-createAsynchronousServiceBusSender
    }

    /**
     * Code sample for creating an asynchronous Service Bus receiver.
     */
    public void createAsynchronousServiceBusReceiver() {
        // BEGIN: readme-sample-createAsynchronousServiceBusReceiver
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
            .receiver()
            .topicName("<< TOPIC NAME >>")
            .subscriptionName("<< SUBSCRIPTION NAME >>")
            .buildAsyncClient();
        // END: readme-sample-createAsynchronousServiceBusReceiver
    }

    /**
     * Code sample for creating an asynchronous Service Bus receiver using {@link DefaultAzureCredentialBuilder}.
     */
    public void createAsynchronousServiceBusReceiverWithAzureIdentity() {
        // BEGIN: readme-sample-createAsynchronousServiceBusReceiverWithAzureIdentity
        TokenCredential credential = new DefaultAzureCredentialBuilder()
            .build();
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .credential("<<fully-qualified-namespace>>", credential)
            .receiver()
            .queueName("<<queue-name>>")
            .buildAsyncClient();
        // END: readme-sample-createAsynchronousServiceBusReceiverWithAzureIdentity
    }

    /**
     * Code sample for creating an asynchronous Service Bus receiver using {@link AzureNamedKeyCredential}.
     */
    public void createAsynchronousServiceBusReceiverWithAzureNamedKeyCredential() {
        // BEGIN: readme-sample-createAsynchronousServiceBusReceiverWithAzureNamedKeyCredential
        AzureNamedKeyCredential azureNamedKeyCredential =
            new AzureNamedKeyCredential("<<azure-service-sas-key-name>>", "<<azure-service-sas-key>>");
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .credential(azureNamedKeyCredential)
            .receiver()
            .queueName("<<queue-name>>")
            .buildAsyncClient();
        // END: readme-sample-createAsynchronousServiceBusReceiverWithAzureNamedKeyCredential
    }

    /**
     * Sends messages to a queue.
     */
    public void sendMessage() {
        // BEGIN: readme-sample-sendMessage
        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
            .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
            .sender()
            .queueName("<< QUEUE NAME >>")
            .buildClient();
        List<ServiceBusMessage> messages = Arrays.asList(
            new ServiceBusMessage("Hello world").setMessageId("1"),
            new ServiceBusMessage("Bonjour").setMessageId("2"));

        sender.sendMessages(messages);

        // When you are done using the sender, dispose of it.
        sender.close();
        // END: readme-sample-sendMessage
    }

    /**
     * Receives messages from a topic and subscription.
     */
    public void receiveMessages() {
        ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
            .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
            .receiver()
            .topicName("<< TOPIC NAME >>")
            .subscriptionName("<< SUBSCRIPTION NAME >>")
            .buildClient();

        // Receives a batch of messages when 10 messages are received or until 30 seconds have elapsed, whichever
        // happens first.
        IterableStream<ServiceBusReceivedMessage> messages = receiver.receiveMessages(10, Duration.ofSeconds(30));
        messages.forEach(message -> {

            System.out.printf("Id: %s. Contents: %s%n", message.getMessageId(), message.getBody());
        });

        // When you are done using the receiver, dispose of it.
        receiver.close();
    }

    /**
     * Receives messages asynchronously.
     */
    public void receiveMessagesAsync() {
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
            .receiver()
            .queueName("<< QUEUE NAME >>")
            .buildAsyncClient();

        // receive() operation continuously fetches messages until the subscription is disposed.
        // The stream is infinite, and completes when the subscription or receiver is closed.
        Disposable subscription = receiver.receiveMessages().subscribe(message -> {

            System.out.printf("Id: %s%n", message.getMessageId());
            System.out.printf("Contents: %s%n", message.getBody().toString());
        }, error -> System.err.println("Error occurred while receiving messages: " + error),
            () -> System.out.println("Finished receiving messages."));

        // Continue application processing. When you are finished receiving messages, dispose of the subscription.
        subscription.dispose();

        // When you are done using the receiver, dispose of it.
        receiver.close();
    }

    /**
     * Complete a message.
     */
    public void completeMessage() {

        ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
            .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
            .receiver()
            .topicName("<< TOPIC NAME >>")
            .subscriptionName("<< SUBSCRIPTION NAME >>")
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .buildClient();

        // This fetches a batch of 10 messages or until the default operation timeout has elapsed.
        receiver.receiveMessages(10).forEach(message -> {
            // Process message and then complete it.
            System.out.println("Completing message " + message.getLockToken());

            receiver.complete(message);
        });
    }

    /**
     * Create a session message.
     */
    public void createSessionMessage() {
        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
            .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
            .sender()
            .queueName("<< QUEUE NAME >>")
            .buildClient();

        // BEGIN: readme-sample-createSessionMessage
        // Setting sessionId publishes that message to a specific session, in this case, "greeting".
        ServiceBusMessage message = new ServiceBusMessage("Hello world")
            .setSessionId("greetings");

        sender.sendMessage(message);
        // END: readme-sample-createSessionMessage
    }

    /**
     * Create session receiver for "greetings"
     */
    public void namedSessionReceiver() {
        // Creates a session-enabled receiver that gets messages from the session "greetings".
        ServiceBusSessionReceiverAsyncClient sessionReceiver = new ServiceBusClientBuilder()
            .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
            .sessionReceiver()
            .queueName("<< QUEUE NAME >>")
            .buildAsyncClient();
        Mono<ServiceBusReceiverAsyncClient> receiverAsyncClient = sessionReceiver.acceptSession("greetings");
    }

    /**
     * Create session receiver for the first available session.
     */
    public void unnamedSessionReceiver() {
        // Creates a session-enabled receiver that gets messages from the first available session.
        ServiceBusSessionReceiverAsyncClient sessionReceiver = new ServiceBusClientBuilder()
            .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
            .sessionReceiver()
            .queueName("<< QUEUE NAME >>")
            .buildAsyncClient();
        Mono<ServiceBusReceiverAsyncClient> receiverAsyncClient = sessionReceiver.acceptNextSession();
    }

    /**
     * Code sample for creating a synchronous Service Bus receiver to read message from dead-letter queue.
     */
    public void createSynchronousServiceBusDeadLetterQueueReceiver() {
        // BEGIN: readme-sample-createSynchronousServiceBusDeadLetterQueueReceiver
        ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
            .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
            .receiver() // Use this for session or non-session enabled queue or topic/subscriptions
            .topicName("<< TOPIC NAME >>")
            .subscriptionName("<< SUBSCRIPTION NAME >>")
            .subQueue(SubQueue.DEAD_LETTER_QUEUE)
            .buildClient();
        // END: readme-sample-createSynchronousServiceBusDeadLetterQueueReceiver
    }

    /**
     * Code sample for creating a Service Bus Processor Client to receive in PeekLock mode.
     */
    public void createServiceBusProcessorClientInPeekLockMode() {
        // BEGIN: readme-sample-createServiceBusProcessorClientInPeekLockMode
        // Sample code that processes a single message which is received in PeekLock mode.
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
                                        .disableAutoComplete() // Make sure to explicitly opt in to manual settlement (e.g. complete, abandon).
                                        .processMessage(processMessage)
                                        .processError(processError)
                                        .disableAutoComplete()
                                        .buildProcessorClient();

        // Starts the processor in the background and returns immediately
        processorClient.start();
        // END: readme-sample-createServiceBusProcessorClientInPeekLockMode
    }

    /**
     * Code sample for creating a Service Bus Processor Client to receive in ReceiveAndDelete mode.
     */
    public void createServiceBusProcessorClientInReceiveAndDelete() {
        // BEGIN: readme-sample-createServiceBusProcessorClientInReceiveAndDeleteMode
        // Sample code that processes a single message which is received in ReceiveAndDelete mode.
        Consumer<ServiceBusReceivedMessageContext> processMessage = context -> {
            final ServiceBusReceivedMessage message = context.getMessage();
            System.out.printf("handler processing message. Session: %s, Sequence #: %s. Contents: %s%n", message.getMessageId(),
                message.getSequenceNumber(), message.getBody());
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
        // END: readme-sample-createServiceBusProcessorClientInReceiveAndDeleteMode
    }

    public void connectionSharingAcrossClients() {
        // BEGIN: readme-sample-connectionSharingAcrossClients
        // Create shared builder.
        ServiceBusClientBuilder sharedConnectionBuilder = new ServiceBusClientBuilder()
            .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>");
        // Create receiver and sender which will share the connection.
        ServiceBusReceiverClient receiver = sharedConnectionBuilder
            .receiver()
            .queueName("<< QUEUE NAME >>")
            .buildClient();
        ServiceBusSenderClient sender = sharedConnectionBuilder
            .sender()
            .queueName("<< QUEUE NAME >>")
            .buildClient();
        // END: readme-sample-connectionSharingAcrossClients
    }

}
