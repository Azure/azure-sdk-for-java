// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

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
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS ARE USED TO EXTRACT
 * APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING LINE NUMBERS OF EXISTING CODE
 * SAMPLES.
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {
    /**
     * Code sample for creating an asynchronous Service Bus sender.
     */
    public void createAsynchronousServiceBusSender() {
        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
            .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
            .sender()
            .queueName("<< QUEUE NAME >>")
            .buildClient();
    }

    /**
     * Code sample for creating an asynchronous Service Bus receiver.
     */
    public void createAsynchronousServiceBusReceiver() {
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
            .receiver()
            .topicName("<< TOPIC NAME >>")
            .subscriptionName("<< SUBSCRIPTION NAME >>")
            .buildAsyncClient();
    }

    /**
     * Code sample for creating an asynchronous Service Bus receiver using {@link DefaultAzureCredentialBuilder}.
     */
    public void createAsynchronousServiceBusReceiverWithAzureIdentity() {
        TokenCredential credential = new DefaultAzureCredentialBuilder()
            .build();
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .credential("<<fully-qualified-namespace>>", credential)
            .receiver()
            .queueName("<<queue-name>>")
            .buildAsyncClient();
    }

    /**
     * Sends messages to a queue.
     */
    public void sendMessage() {
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

            System.out.printf("Id: %s. Contents: %s%n", message.getMessageId(),
                message.getBody().toString());
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
        }, error -> {
                System.err.println("Error occurred while receiving messages: " + error);
            }, () -> {
                System.out.println("Finished receiving messages.");
            });

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

        // Setting sessionId publishes that message to a specific session, in this case, "greeting".
        ServiceBusMessage message = new ServiceBusMessage("Hello world")
            .setSessionId("greetings");

        sender.sendMessage(message);
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
     * Code sample for creating an synchronous Service Bus receiver to read message from dead-letter queue.
     */
    public void createSynchronousServiceBusDeadLetterQueueReceiver() {
        ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
            .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
            .receiver() // Use this for session or non-session enabled queue or topic/subscriptions
            .topicName("<< TOPIC NAME >>")
            .subscriptionName("<< SUBSCRIPTION NAME >>")
            .subQueue(SubQueue.DEAD_LETTER_QUEUE)
            .buildClient();
    }

    /**
     * Code sample for creating a Service Bus Processor Client.
     */
    public void createServiceBusProcessorClient() {
        // Sample code that processes a single message
        Consumer<ServiceBusReceivedMessageContext> processMessage = messageContext -> {
            try {
                System.out.println(messageContext.getMessage().getMessageId());
                // other message processing code
                messageContext.complete();
            } catch (Exception ex) {
                messageContext.abandon();
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
                                        .processMessage(processMessage)
                                        .processError(processError)
                                        .disableAutoComplete()
                                        .buildProcessorClient();

        // Starts the processor in the background and returns immediately
        processorClient.start();
    }

    public void connectionSharingAcrossClients() {
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
    }

}
