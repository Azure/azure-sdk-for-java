// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.IterableStream;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.models.AbandonOptions;
import com.azure.messaging.servicebus.models.CompleteOptions;
import com.azure.messaging.servicebus.models.SubQueue;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Code snippets demonstrating various {@link ServiceBusReceiverClient} and {@link ServiceBusReceiverAsyncClient}.
 */
public class ServiceBusReceiverClientJavaDocCodeSample {
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
     * Name of a topic inside the Service Bus namespace.
     */
    private final String topicName = System.getenv("AZURE_SERVICEBUS_SAMPLE_TOPIC_NAME");
    /**
     * Name of a subscription associated with the {@link #topicName}.
     */
    private final String subscriptionName = System.getenv("AZURE_SERVICEBUS_SAMPLE_SUBSCRIPTION_NAME");
    /**
     * Name of a session-enabled queue in the Service Bus namespace.
     */
    private final String sessionEnabledQueueName = System.getenv("AZURE_SERVICEBUS_SAMPLE_SESSION_QUEUE_NAME");

    /**
     * Code snippet for creating an ServiceBusReceiverClient
     */
    @Test
    public void instantiate() {
        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverclient.instantiation
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        // 'disableAutoComplete' indicates that users will explicitly settle their message.
        ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, credential)
            .receiver()
            .disableAutoComplete()
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .buildClient();

        receiver.receiveMessages(3, Duration.ofSeconds(5))
            .forEach(message -> {
                System.out.println("Message: " + message.getBody());
            });

        // Use the receiver and finally close it.
        receiver.close();
        // END: com.azure.messaging.servicebus.servicebusreceiverclient.instantiation
    }

    /**
     * Code snippet for creating an ServiceBusReceiverClient
     */
    @Test
    public void instantiateAsync() {
        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        // 'disableAutoComplete' indicates that users will explicitly settle their message.
        ServiceBusReceiverAsyncClient asyncReceiver = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, credential)
            .receiver()
            .disableAutoComplete()
            .queueName(queueName)
            .buildAsyncClient();

        // Use the receiver and finally close it.
        asyncReceiver.close();
        // END: com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation
    }

    /**
     * Code sample for creating a synchronous Service Bus receiver to read message from dead-letter queue.
     */
    public void instantiateDeadLetterQueue() {
        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverclient.instantiation-deadLetterQueue
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        // 'disableAutoComplete' indicates that users will explicitly settle their message.
        ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, credential)
            .receiver() // Use this for session or non-session enabled queue or topic/subscriptions
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .subQueue(SubQueue.DEAD_LETTER_QUEUE)
            .buildClient();

        // Use the receiver and finally close it.
        receiver.close();
        // END: com.azure.messaging.servicebus.servicebusreceiverclient.instantiation-deadLetterQueue
    }

    /**
     * Receives messages from a topic and subscription.
     */
    public void receiveMessages() {
        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverclient.receiveMessages-int-duration
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
        // END: com.azure.messaging.servicebus.servicebusreceiverclient.receiveMessages-int-duration
    }

    /**
     * Demonstrates how to create a session receiver for a single, first available session.
     */
    public void sessionReceiverSingleInstantiation() {
        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#nextsession
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        // 'disableAutoComplete' indicates that users will explicitly settle their message.
        ServiceBusSessionReceiverClient sessionReceiver = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, credential)
            .sessionReceiver()
            .disableAutoComplete()
            .queueName(queueName)
            .buildClient();

        // Creates a client to receive messages from the first available session. It waits until
        // AmqpRetryOptions.getTryTimeout() elapses. If no session is available within that operation timeout, it
        // throws a retriable error. Otherwise, a receiver is returned when a lock on the session is acquired.
        ServiceBusReceiverClient receiver = sessionReceiver.acceptNextSession();

        // Use the receiver and finally close it along with the sessionReceiver.
        try {
            IterableStream<ServiceBusReceivedMessage> receivedMessages =
                receiver.receiveMessages(10, Duration.ofSeconds(30));

            for (ServiceBusReceivedMessage message : receivedMessages) {
                System.out.println("Body: " + message);
            }
        } finally {
            receiver.close();
            sessionReceiver.close();
        }

        // END: com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#nextsession
    }

    /**
     * Demonstrates how to create a session receiver for a single, first available session for
     * {@link ServiceBusReceiverAsyncClient}.
     */
    public void sessionReceiverSingleInstantiationAsync() {
        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation#nextsession
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        // 'disableAutoComplete' indicates that users will explicitly settle their message.
        ServiceBusSessionReceiverAsyncClient sessionReceiver = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, credential)
            .sessionReceiver()
            .disableAutoComplete()
            .queueName(queueName)
            .buildAsyncClient();

        // Creates a client to receive messages from the first available session. It waits until
        // AmqpRetryOptions.getTryTimeout() elapses. If no session is available within that operation timeout, it
        // completes with a retriable error. Otherwise, a receiver is returned when a lock on the session is acquired.
        Mono<ServiceBusReceiverAsyncClient> receiverMono = sessionReceiver.acceptNextSession();

        Flux.usingWhen(receiverMono,
                receiver -> receiver.receiveMessages(),
                receiver -> Mono.fromRunnable(() -> {
                    // Dispose of the receiver and sessionReceiver when done receiving messages.
                    receiver.close();
                    sessionReceiver.close();
                }))
            .subscribe(message -> {
                System.out.println("Received message: " + message.getBody());
            });
        // END: com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation#nextsession
    }

    /**
     * Demonstrates how to create a session receiver for a single know session id.
     */
    @Test
    public void sessionReceiverSessionIdInstantiation() {
        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#sessionId
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        // 'disableAutoComplete' indicates that users will explicitly settle their message.
        ServiceBusSessionReceiverClient sessionReceiver = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, credential)
            .sessionReceiver()
            .queueName(sessionEnabledQueueName)
            .disableAutoComplete()
            .buildClient();
        ServiceBusReceiverClient receiver = sessionReceiver.acceptSession("<<my-session-id>>");

        // Use the receiver and finally close it along with the sessionReceiver.
        receiver.close();
        sessionReceiver.close();
        // END: com.azure.messaging.servicebus.servicebusreceiverclient.instantiation#sessionId
    }

    /**
     * Demonstrates how to use a transaction.
     */
    @Test
    public void transactionsSnippet() {
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, credential)
            .receiver()
            .disableAutoComplete()
            .queueName(queueName)
            .buildClient();

        // Some random sequenceNumber.
        long sequenceNumber = 1000L;
        ServiceBusReceivedMessage receivedMessage = new ServiceBusReceivedMessage((BinaryData) null);

        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverclient.committransaction#servicebustransactioncontext
        ServiceBusTransactionContext transaction = receiver.createTransaction();

        // Process messages and associate operations with the transaction.
        ServiceBusReceivedMessage deferredMessage = receiver.receiveDeferredMessage(sequenceNumber);
        receiver.complete(deferredMessage, new CompleteOptions().setTransactionContext(transaction));
        receiver.abandon(receivedMessage, new AbandonOptions().setTransactionContext(transaction));
        receiver.commitTransaction(transaction);
        // END: com.azure.messaging.servicebus.servicebusreceiverclient.committransaction#servicebustransactioncontext

        receiver.close();
    }

    @Test
    public void connectionSharingAcrossClients() {
        // BEGIN: com.azure.messaging.servicebus.connection.sharing
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // Retrieve 'connectionString' and 'queueName' from your configuration.
        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        ServiceBusClientBuilder sharedConnectionBuilder = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, credential);

        // Create receiver and sender which will share the connection.
        ServiceBusReceiverClient receiver = sharedConnectionBuilder
            .receiver()
            .queueName(queueName)
            .buildClient();
        ServiceBusSenderClient sender = sharedConnectionBuilder
            .sender()
            .queueName(queueName)
            .buildClient();

        // Use the clients and finally close them.
        try {
            sender.sendMessage(new ServiceBusMessage("payload"));
            receiver.receiveMessages(1);
        } finally {
            sender.close();
            receiver.close();
        }
        // END: com.azure.messaging.servicebus.connection.sharing
    }
}
