// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.IterableStream;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.models.AbandonOptions;
import com.azure.messaging.servicebus.models.CompleteOptions;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.messaging.servicebus.models.SubQueue;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Code snippets demonstrating various {@link ServiceBusReceiverClient} and {@link ServiceBusReceiverAsyncClient}.
 */
public class ServiceBusReceiverClientJavaDocCodeSamples {
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
    @Test
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
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        ServiceBusReceiverClient receiver = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, tokenCredential)
            .receiver()
            .topicName(topicName)
            .subscriptionName(subscriptionName)
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
     * Receives from all the messages.
     */
    @Test
    public void receiveMessagesAsync() {
        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        // 'disableAutoComplete' indicates that users will explicitly settle their message.
        ServiceBusReceiverAsyncClient asyncReceiver = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, new DefaultAzureCredentialBuilder().build())
            .receiver()
            .disableAutoComplete()
            .queueName(queueName)
            .buildAsyncClient();

        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverasyncclient.receiveMessages
        // Keep a reference to `subscription`. When the program is finished receiving messages, call
        // subscription.dispose(). This will stop fetching messages from the Service Bus.
        // Consider using Flux.usingWhen to scope the creation, usage, and cleanup of the receiver.
        Disposable subscription = asyncReceiver.receiveMessages()
            .subscribe(message -> {
                System.out.printf("Received Seq #: %s%n", message.getSequenceNumber());
                System.out.printf("Contents of message as string: %s%n", message.getBody());
            },
                error -> System.out.println("Error occurred: " + error),
                () -> System.out.println("Receiving complete."));

        // When program ends, or you're done receiving all messages.
        subscription.dispose();
        asyncReceiver.close();
        // END: com.azure.messaging.servicebus.servicebusreceiverasyncclient.receiveMessages
    }

    /**
     * Receives message from a queue or topic using receive and delete mode.
     */
    @Test
    public void receiveWithReceiveAndDeleteModeAsync() {
        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverasyncclient.receiveWithReceiveAndDeleteMode
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // Keep a reference to `subscription`. When the program is finished receiving messages, call
        // subscription.dispose(). This will stop fetching messages from the Service Bus.
        Disposable subscription = Flux.usingWhen(
                Mono.fromCallable(() -> {
                    // Setting the receiveMode when creating the receiver enables receive and delete mode. By default,
                    // peek lock mode is used. In peek lock mode, users are responsible for settling messages.
                    return new ServiceBusClientBuilder()
                        .credential(fullyQualifiedNamespace, credential)
                        .receiver()
                        .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
                        .queueName(queueName)
                        .buildAsyncClient();
                }), receiver -> {
                    return receiver.receiveMessages();
                }, receiver -> {
                    return Mono.fromRunnable(() -> receiver.close());
                })
            .subscribe(message -> {
                System.out.printf("Received Seq #: %s%n", message.getSequenceNumber());
                System.out.printf("Contents of message as string: %s%n", message.getBody().toString());
            }, error -> System.err.print(error));
        // END: com.azure.messaging.servicebus.servicebusreceiverasyncclient.receiveWithReceiveAndDeleteMode

        subscription.dispose();
    }

    /**
     * Receives message with back pressure.
     */
    @Test
    public void receiveMessagesBackpressureAsync() {
        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        // 'disableAutoComplete' indicates that users will explicitly settle their message.
        ServiceBusReceiverAsyncClient asyncReceiver = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, new DefaultAzureCredentialBuilder().build())
            .receiver()
            .disableAutoComplete()
            .queueName(queueName)
            .buildAsyncClient();

        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverasyncclient.receive#basesubscriber
        // This is a non-blocking call. The program will move to the next line of code after setting up the operation.
        asyncReceiver.receiveMessages().subscribe(new BaseSubscriber<ServiceBusReceivedMessage>() {
            private static final int NUMBER_OF_MESSAGES = 5;
            private final AtomicInteger currentNumberOfMessages = new AtomicInteger();

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                // Tell the Publisher we only want 5 message at a time.
                request(NUMBER_OF_MESSAGES);
            }

            @Override
            protected void hookOnNext(ServiceBusReceivedMessage message) {
                // Process the ServiceBusReceivedMessage
                // If the number of messages we have currently received is a multiple of 5, that means we have reached
                // the last message the Subscriber will provide to us. Invoking request(long) here, tells the Publisher
                // that the subscriber is ready to get more messages from upstream.
                if (currentNumberOfMessages.incrementAndGet() % 5 == 0) {
                    request(NUMBER_OF_MESSAGES);
                }
            }
        });
        // END: com.azure.messaging.servicebus.servicebusreceiverasyncclient.receive#basesubscriber

        // When completed receiving messages, close the receiver.
        asyncReceiver.close();
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
            .queueName(sessionEnabledQueueName)
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
            .queueName(sessionEnabledQueueName)
            .buildAsyncClient();

        // Creates a client to receive messages from the first available session. It waits until
        // AmqpRetryOptions.getTryTimeout() elapses. If no session is available within that operation timeout, it
        // completes with a retriable error. Otherwise, a receiver is returned when a lock on the session is acquired.
        Mono<ServiceBusReceiverAsyncClient> receiverMono = sessionReceiver.acceptNextSession();

        Disposable disposable = Flux.usingWhen(receiverMono,
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

        // Users can dispose of the subscription to cancel receive operation.
        disposable.dispose();
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
     * Demonstrates how to create a session receiver for a single know session id.
     */
    @Test
    public void sessionReceiverSessionIdInstantiationAsync() {
        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation#sessionId
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        // 'disableAutoComplete' indicates that users will explicitly settle their message.
        ServiceBusSessionReceiverAsyncClient sessionReceiver = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, credential)
            .sessionReceiver()
            .disableAutoComplete()
            .queueName(sessionEnabledQueueName)
            .buildAsyncClient();

        // acceptSession(String) completes successfully with a receiver when "<<my-session-id>>" session is
        // successfully locked.
        // `Flux.usingWhen` is used, so we dispose of the receiver resource after `receiveMessages()` completes.
        // `Mono.usingWhen` can also be used if the resource closure only returns a single item.
        Flux<ServiceBusReceivedMessage> sessionMessages = Flux.usingWhen(
            sessionReceiver.acceptSession("<<my-session-id>>"),
            receiver -> {
                // Receive messages from <<my-session-id>> session.
                return receiver.receiveMessages();
            },
            receiver -> Mono.fromRunnable(() -> {
                // Dispose of
                receiver.close();
                sessionReceiver.close();
            }));

        // When program ends, or you're done receiving all messages, the `subscription` can be disposed of. This code
        // is non-blocking and kicks off the operation.
        Disposable subscription = sessionMessages.subscribe(
            message -> System.out.printf("Received Sequence #: %s. Contents: %s%n",
                message.getSequenceNumber(), message.getBody()),
            error -> System.err.print(error),
            () -> System.out.println("Completed receiving from session."));
        // END: com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation#sessionId

        subscription.dispose();
        sessionReceiver.close();
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
        ServiceBusReceivedMessage receivedMessage = new ServiceBusReceivedMessage(BinaryData.fromString("Hello"));

        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverclient.committransaction#servicebustransactioncontext
        ServiceBusTransactionContext transaction = receiver.createTransaction();

        // Process messages and associate operations with the transaction.
        ServiceBusReceivedMessage deferredMessage = receiver.receiveDeferredMessage(sequenceNumber);
        receiver.complete(deferredMessage, new CompleteOptions().setTransactionContext(transaction));
        receiver.abandon(receivedMessage, new AbandonOptions().setTransactionContext(transaction));
        receiver.commitTransaction(transaction);
        // END: com.azure.messaging.servicebus.servicebusreceiverclient.committransaction#servicebustransactioncontext

        // Close receiver when finished using it.
        receiver.close();
    }

    /**
     * Demonstrates transactions using async client.
     */
    @Test
    public void transactionsSnippetAsync() {
        ServiceBusReceiverAsyncClient asyncReceiver = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, new DefaultAzureCredentialBuilder().build())
            .receiver()
            .disableAutoComplete()
            .queueName(queueName)
            .buildAsyncClient();

        // Some random sequenceNumber.
        long sequenceNumber = 1000L;
        ServiceBusReceivedMessage receivedMessage = new ServiceBusReceivedMessage(BinaryData.fromString("Hello"));

        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverasyncclient.committransaction#servicebustransactioncontext
        // This mono creates a transaction and caches the output value, so we can associate operations with the
        // transaction. It does not cache the value if it is an error or completes with no items, effectively retrying
        // the operation.
        Mono<ServiceBusTransactionContext> transactionContext = asyncReceiver.createTransaction()
            .cache(value -> Duration.ofMillis(Long.MAX_VALUE),
                error -> Duration.ZERO,
                () -> Duration.ZERO);

        // Dispose of the disposable to cancel the operation.
        Disposable disposable = transactionContext.flatMap(transaction -> {
            // Process messages and associate operations with the transaction.
            Mono<Void> operations = Mono.when(
                asyncReceiver.receiveDeferredMessage(sequenceNumber).flatMap(message ->
                    asyncReceiver.complete(message, new CompleteOptions().setTransactionContext(transaction))),
                asyncReceiver.abandon(receivedMessage, new AbandonOptions().setTransactionContext(transaction)));

            // Finally, either commit or rollback the transaction once all the operations are associated with it.
            return operations.then(asyncReceiver.commitTransaction(transaction));
        }).subscribe(unused -> {
        }, error -> {
            System.err.println("Error occurred processing transaction: " + error);
        }, () -> {
            System.out.println("Completed transaction");
        });
        // END: com.azure.messaging.servicebus.servicebusreceiverasyncclient.committransaction#servicebustransactioncontext
        // Close receiver when finished using it.
        asyncReceiver.close();

        if (!disposable.isDisposed()) {
            disposable.dispose();
        }
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
