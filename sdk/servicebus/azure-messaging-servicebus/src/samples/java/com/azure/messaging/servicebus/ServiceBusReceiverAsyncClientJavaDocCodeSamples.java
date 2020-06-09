// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.models.ReceiveMode;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Code snippets demonstrating various {@link ServiceBusReceiverAsyncClient} scenarios.
 */
public class ServiceBusReceiverAsyncClientJavaDocCodeSamples {
    public void initialization() {
        // BEGIN: com.azure.messaging.servicebus.servicebusasyncreceiverclient.instantiation
        // The required parameters is connectionString, a way to authenticate with Service Bus using credentials.
        ServiceBusReceiverAsyncClient consumer = new ServiceBusClientBuilder()
            .connectionString("Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};"
                + "SharedAccessKey={key};EntityPath={eh-name}")
            .receiver()
            .queueName("<< QUEUE NAME >>")
            .buildAsyncClient();
        // END: com.azure.messaging.servicebus.servicebusasyncreceiverclient.instantiation

        consumer.close();
    }

    public void instantiateWithDefaultCredential() {
        // BEGIN: com.azure.messaging.servicebus.servicebusasyncreceiverclient.instantiateWithDefaultCredential
        // The required parameters is connectionString, a way to authenticate with Service Bus using credentials.
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .credential("<<fully-qualified-namespace>>",
                new DefaultAzureCredentialBuilder().build())
            .receiver()
            .queueName("<< QUEUE NAME >>")
            .buildAsyncClient();
        // END: com.azure.messaging.servicebus.servicebusasyncreceiverclient.instantiateWithDefaultCredential

        receiver.close();
    }

    /**
     * Receives message from a queue or topic using receive and delete mode.
     */
    public void receiveWithReceiveAndDeleteMode() {
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString("fake-string")
            .receiver()
            .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
            .queueName("<< QUEUE NAME >>")
            .buildAsyncClient();
        // BEGIN: com.azure.messaging.servicebus.servicebusasyncreceiverclient.receiveWithReceiveAndDeleteMode

        // Keep a reference to `subscription`. When the program is finished receiving messages, call
        // subscription.dispose(). This will stop fetching messages from the Service Bus.
        Disposable subscription = receiver.receive()
            .subscribe(context -> {
                ServiceBusReceivedMessage message = context.getMessage();
                System.out.printf("Received message id: %s%n", message.getMessageId());
                System.out.printf("Contents of message as string: %s%n", new String(message.getBody(), UTF_8));
            }, error -> System.err.print(error));
        // END: com.azure.messaging.servicebus.servicebusasyncreceiverclient.receiveWithReceiveAndDeleteMode

        // When program ends, or you're done receiving all messages.
        receiver.close();
        subscription.dispose();
    }

    /**
     * Receives message with back pressure.
     */
    public void receiveBackpressure() {

        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString("fake-string")
            .receiver()
            .queueName("<< QUEUE NAME >>")
            .buildAsyncClient();

        // BEGIN: com.azure.messaging.servicebus.servicebusasyncreceiverclient.receive#basesubscriber
        receiver.receive().subscribe(new BaseSubscriber<ServiceBusReceivedMessageContext>() {
            private static final int NUMBER_OF_MESSAGES = 5;
            private final AtomicInteger currentNumberOfMessages = new AtomicInteger();

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                // Tell the Publisher we only want 5 message at a time.
                request(NUMBER_OF_MESSAGES);
            }

            @Override
            protected void hookOnNext(ServiceBusReceivedMessageContext value) {
                // Process the ServiceBusReceivedMessage
                ServiceBusReceivedMessage message = value.getMessage();

                // If the number of messages we have currently received is a multiple of 5, that means we have reached
                // the last message the Subscriber will provide to us. Invoking request(long) here, tells the Publisher
                // that the subscriber is ready to get more messages from upstream.
                if (currentNumberOfMessages.incrementAndGet() % 5 == 0) {
                    request(NUMBER_OF_MESSAGES);
                }
            }
        });
        // END: com.azure.messaging.servicebus.servicebusasyncreceiverclient.receive#basesubscriber
        receiver.close();
    }

    /**
     * Receives from all the messages.
     */
    public void receiveAll() {
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString("fake-string")
            .receiver()
            .queueName("<< QUEUE NAME >>")
            .buildAsyncClient();

        // BEGIN: com.azure.messaging.servicebus.servicebusasyncreceiverclient.receive#all
        Disposable subscription = receiver.receive().flatMap(context -> {
            ServiceBusReceivedMessage message = context.getMessage();
            System.out.printf("Received message id: %s%n", message.getMessageId());
            System.out.printf("Contents of message as string: %s%n", new String(message.getBody(), UTF_8));
            return receiver.complete(message);
        }).subscribe(aVoid -> System.out.println("Processed message."),
            error -> System.out.println("Error occurred: " + error));

        // When program ends, or you're done receiving all messages.
        subscription.dispose();
        receiver.close();
        // END: com.azure.messaging.servicebus.servicebusasyncreceiverclient.receive#all
    }

    /**
     * Receives messages up to a time or duration.
     */
    public void receiveMaxTimeDuration() {
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString("fake-string")
            .receiver()
            .queueName("<< QUEUE NAME >>")
            .buildAsyncClient();

        // BEGIN: com.azure.messaging.servicebus.servicebusasyncreceiverclient.receive#int-duration
        Disposable subscription = receiver.receive(15, Duration.ofSeconds(30))
            .flatMap(context -> {
                ServiceBusReceivedMessage message = context.getMessage();
                System.out.printf("Received message id: %s%n", message.getMessageId());
                System.out.printf("Contents of message as string: %s%n", new String(message.getBody(), UTF_8));
                return receiver.complete(message);
            }).subscribe(aVoid -> System.out.println("Processed message."),
                error -> System.out.println("Error occurred: " + error));

        // subscribe is a non-blocking call and program flow will continue before messages are fetched.
        // END: com.azure.messaging.servicebus.servicebusasyncreceiverclient.receive#int-duration

        // When program ends, or you're done receiving all messages.
        subscription.dispose();

        receiver.close();
    }

    /**
     * Demonstrates how to create a session receiver for a single, first available session.
     */
    public void sessionReceiverSingleInstantiation() {
        // BEGIN: com.azure.messaging.servicebus.servicebusasyncreceiverclient.instantiation#singlesession
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString("Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};"
                + "SharedAccessKey={key};EntityPath={eh-name}")
            .sessionReceiver()
            .queueName("<< QUEUE NAME >>")
            .buildAsyncClient();
        // END: com.azure.messaging.servicebus.servicebusasyncreceiverclient.instantiation#singlesession

        receiver.close();
    }

    /**
     * Demonstrates how to create a session receiver for a specific session.
     */
    public void sessionReceiverNamedInstantiation() {
        // BEGIN: com.azure.messaging.servicebus.servicebusasyncreceiverclient.instantiation#sessionId
        ServiceBusReceiverAsyncClient consumer = new ServiceBusClientBuilder()
            .connectionString("Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};"
                + "SharedAccessKey={key};EntityPath={eh-name}")
            .sessionReceiver()
            .topicName("<< TOPIC NAME >>")
            .subscriptionName("<< SUBSCRIPTION NAME >>")
            .sessionId("<< my-session-id >>")
            .buildAsyncClient();
        // END: com.azure.messaging.servicebus.servicebusasyncreceiverclient.instantiation#sessionId

        consumer.close();
    }

    /**
     * Demonstrates how to create a session receiver for processing multiple sessions.
     */
    public void sessionReceiverMultipleInstantiation() {
        // BEGIN: com.azure.messaging.servicebus.servicebusasyncreceiverclient.instantiation#multiplesessions
        ServiceBusReceiverAsyncClient consumer = new ServiceBusClientBuilder()
            .connectionString("Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};"
                + "SharedAccessKey={key};EntityPath={eh-name}")
            .sessionReceiver()
            .topicName("<< TOPIC NAME >>")
            .subscriptionName("<< SUBSCRIPTION NAME >>")
            .maxConcurrentSessions(3)
            .buildAsyncClient();
        // END: com.azure.messaging.servicebus.servicebusasyncreceiverclient.instantiation#multiplesessions

        consumer.close();
    }

    public void createTransaction() {
        // The required parameters is connectionString, a way to authenticate with Service Bus using credentials.
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString("Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};"
                + "SharedAccessKey={key};EntityPath={eh-name}")
            .receiver()
            .queueName("<< QUEUE NAME >>")
            .buildAsyncClient();

        // BEGIN: com.azure.messaging.servicebus.servicebusasyncreceiverclient.createTransaction
        // Hold the transaction.
        AtomicReference<ServiceBusTransactionContext> transaction = new AtomicReference<>();

        // Keep a reference to `subscription`. When the program is finished receiving messages, call
        // subscription.dispose(). This will dispose it cleanly.
        Disposable subscriber = receiver.createTransaction()
            .flatMap(transactionContext -> {
                transaction.set(transactionContext);
                System.out.println("Transaction is created.");
                return Mono.empty();
            })
            .subscribe();

        // END: com.azure.messaging.servicebus.servicebusasyncreceiverclient.createTransaction

        receiver.close();
    }

    public void commitTransaction() {
        // The required parameters is connectionString, a way to authenticate with Service Bus using credentials.
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString("Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};"
                + "SharedAccessKey={key};EntityPath={eh-name}")
            .receiver()
            .queueName("<< QUEUE NAME >>")
            .buildAsyncClient();
        ServiceBusTransactionContext transactionContext = null;

        // BEGIN: com.azure.messaging.servicebus.servicebusasyncreceiverclient.commitTransaction
        // transactionContext: This is the transaction which you have created previously.

        // Keep a reference to `subscription`. When the program is finished receiving messages, call
        // subscription.dispose(). This will dispose it cleanly.
        Disposable subscriber = receiver.commitTransaction(transactionContext)
            .subscribe();
        // END: com.azure.messaging.servicebus.servicebusasyncreceiverclient.commitTransaction

        receiver.close();
    }

    public void rollbackTransaction() {
        // The required parameters is connectionString, a way to authenticate with Service Bus using credentials.
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString("Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};"
                + "SharedAccessKey={key};EntityPath={eh-name}")
            .receiver()
            .queueName("<< QUEUE NAME >>")
            .buildAsyncClient();
        ServiceBusTransactionContext transactionContext = null;

        // BEGIN: com.azure.messaging.servicebus.servicebusasyncreceiverclient.rollbackTransaction

        // transactionContext: This is the transaction which you have created previously.

        // Keep a reference to `subscription`. When the program is finished receiving messages, call
        // subscription.dispose(). This will dispose it cleanly.
        Disposable subscriber = receiver.rollbackTransaction(transactionContext)
            .subscribe();
        // END: com.azure.messaging.servicebus.servicebusasyncreceiverclient.rollbackTransaction

        receiver.close();
    }

    public void completeMessageWithTransaction() {
        // The required parameters is connectionString, a way to authenticate with Service Bus using credentials.
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString("Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};"
                + "SharedAccessKey={key};EntityPath={eh-name}")
            .receiver()
            .queueName("<< QUEUE NAME >>")
            .buildAsyncClient();

        ServiceBusTransactionContext transactionContext = null;
        ServiceBusReceivedMessageContext messageContext = null;

        // BEGIN: com.azure.messaging.servicebus.servicebusasyncreceiverclient.completeMessageWithTransaction

        // messageContext: This is instance `ServiceBusReceivedMessageContext` which you have received previously.
        // transactionContext: This is the transaction which you have created previously.

        // Keep a reference to `subscription`. When the program is finished receiving messages, call
        // subscription.dispose(). This will dispose it cleanly.
        Disposable subscriber = receiver.complete(messageContext.getMessage(), transactionContext)
            .subscribe();

        // When all the messages are processed and settled, you should commit/rollback this transaction.
        // END: com.azure.messaging.servicebus.servicebusasyncreceiverclient.completeMessageWithTransaction

        receiver.close();
    }

    public void abandonMessageWithTransaction() {
        // The required parameters is connectionString, a way to authenticate with Service Bus using credentials.
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString("Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};"
                + "SharedAccessKey={key};EntityPath={eh-name}")
            .receiver()
            .queueName("<< QUEUE NAME >>")
            .buildAsyncClient();

        ServiceBusTransactionContext transactionContext = null;
        ServiceBusReceivedMessageContext messageContext = null;
        Map<String, Object> propertiesToModify = null;

        // BEGIN: com.azure.messaging.servicebus.servicebusasyncreceiverclient.abandonMessageWithTransaction

        // messageContext: This is instance `ServiceBusReceivedMessageContext` which you have received previously.
        // propertiesToModify : This is Map of any properties to modify while abandoning the message.
        // transactionContext: This is the transaction which you have created previously.

        // Keep a reference to `subscription`. When the program is finished receiving messages, call
        // subscription.dispose(). This will dispose it cleanly.
        Disposable subscriber = receiver.abandon(messageContext.getMessage(), propertiesToModify, transactionContext)
            .subscribe();

        // When all the messages are processed and settled, you should commit/rollback this transaction.
        // END: com.azure.messaging.servicebus.servicebusasyncreceiverclient.abandonMessageWithTransaction

        receiver.close();
    }
}
