// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.IterableStream;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.models.ReceiveMode;
import reactor.core.Disposable;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS ARE USED TO EXTRACT
 * APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING LINE NUMBERS OF EXISTING CODE
 * SAMPLES.
 *
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
            new ServiceBusMessage("Hello world".getBytes()).setMessageId("1"),
            new ServiceBusMessage("Bonjour".getBytes()).setMessageId("2"));

        sender.send(messages);
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
            .receiveMode(ReceiveMode.PEEK_LOCK)
            .buildClient();

        IterableStream<ServiceBusReceivedMessageContext> messages = receiver.receive(10, Duration.ofSeconds(30));
        messages.forEach(context -> {
            ServiceBusReceivedMessage message = context.getMessage();
            System.out.printf("Id: %s. Contents: %s%n", message.getMessageId(),
                new String(message.getBody(), StandardCharsets.UTF_8));
        });
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

        Disposable subscription = receiver.receive().subscribe(context -> {
            ServiceBusReceivedMessage message = context.getMessage();
            System.out.printf("Id: %s%n", message.getMessageId());
            System.out.printf("Contents: %s%n", new String(message.getBody(), StandardCharsets.UTF_8));
        }, error -> {
                System.err.println("Error occurred while receiving messages: " + error);
            }, () -> {
                System.out.println("Finished receiving messages.");
            });
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
            .receiveMode(ReceiveMode.PEEK_LOCK)
            .buildClient();

        receiver.receive(10).forEach(context -> {
            ServiceBusReceivedMessage message = context.getMessage();

            // Process message and then complete it.
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

        ServiceBusMessage message = new ServiceBusMessage("Hello world".getBytes())
            .setSessionId("greetings");

        sender.send(message);
    }

    /**
     * Create session receiver for "greetings"
     */
    public void namedSessionReceiver() {
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
            .sessionReceiver()
            .queueName("<< QUEUE NAME >>")
            .sessionId("greetings")
            .buildAsyncClient();
    }

    /**
     * Create session receiver for the first available session.
     */
    public void unnamedSessionReceiver() {
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString("<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>")
            .sessionReceiver()
            .queueName("<< QUEUE NAME >>")
            .buildAsyncClient();
    }
}
