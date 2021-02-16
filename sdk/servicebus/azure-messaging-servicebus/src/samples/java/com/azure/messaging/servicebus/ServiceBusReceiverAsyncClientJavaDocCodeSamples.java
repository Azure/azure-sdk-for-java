// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.models.AbandonOptions;
import com.azure.messaging.servicebus.models.CompleteOptions;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Code snippets demonstrating various {@link ServiceBusReceiverAsyncClient} scenarios.
 */
public class ServiceBusReceiverAsyncClientJavaDocCodeSamples {
    // The required parameters is connectionString, a way to authenticate with Service Bus using credentials.
    // We are reading 'connectionString/queueName' from environment variable.
    // You can configure them as it fits suitable for your application.
    // 1. "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}"
    // 2. "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
    // 3. "queueName" will be the name of the Service Bus queue instance you created
    //    inside the Service Bus namespace.
    String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
    String queueName = System.getenv("AZURE_SERVICEBUS_SAMPLE_QUEUE_NAME");

    ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()

        .connectionString(connectionString)
        .receiver()
        .queueName(queueName)
        .buildAsyncClient();

    @Test
    public void initialization() {
        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation
        // The required parameters is connectionString, a way to authenticate with Service Bus using credentials.
        // The connectionString/queueName must be set by the application. The 'connectionString' format is shown below.
        // "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}"

        ServiceBusReceiverAsyncClient consumer = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .queueName(queueName)
            .buildAsyncClient();
        // END: com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation

        consumer.close();
    }

    public void instantiateWithDefaultCredential() {
        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiateWithDefaultCredential
        // The required parameters is connectionString, a way to authenticate with Service Bus using credentials.
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .credential("<<fully-qualified-namespace>>",
                new DefaultAzureCredentialBuilder().build())
            .receiver()
            .queueName("<< QUEUE NAME >>")
            .buildAsyncClient();
        // END: com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiateWithDefaultCredential

        receiver.close();
    }

    /**
     * Receives message from a queue or topic using receive and delete mode.
     */
    @Test
    public void receiveWithReceiveAndDeleteMode() {
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
            .queueName(queueName)
            .buildAsyncClient();

        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverasyncclient.receiveWithReceiveAndDeleteMode
        // Keep a reference to `subscription`. When the program is finished receiving messages, call
        // subscription.dispose(). This will stop fetching messages from the Service Bus.
        Disposable subscription = receiver.receiveMessages()
            .subscribe(message -> {
                System.out.printf("Received Seq #: %s%n", message.getSequenceNumber());
                System.out.printf("Contents of message as string: %s%n", message.getBody().toString());
            }, error -> System.err.print(error));
        // END: com.azure.messaging.servicebus.servicebusreceiverasyncclient.receiveWithReceiveAndDeleteMode

        // When program ends, or you're done receiving all messages.
        receiver.close();
        subscription.dispose();
    }

    /**
     * Receives message with back pressure.
     */
    @Test
    public void receiveBackpressure() {
        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverasyncclient.receive#basesubscriber
        receiver.receiveMessages().subscribe(new BaseSubscriber<ServiceBusReceivedMessage>() {
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
        receiver.close();
    }

    /**
     * Receives from all the messages.
     */
    @Test
    public void receiveAll() {
        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverasyncclient.receive#all
        Disposable subscription = receiver.receiveMessages()
            .subscribe(message -> {
                System.out.printf("Received Seq #: %s%n", message.getSequenceNumber());
                System.out.printf("Contents of message as string: %s%n", message.getBody());
            },
                error -> System.out.println("Error occurred: " + error),
                () -> System.out.println("Receiving complete."));

        // When program ends, or you're done receiving all messages.
        subscription.dispose();
        receiver.close();
        // END: com.azure.messaging.servicebus.servicebusreceiverasyncclient.receive#all
    }

    /**
     * Demonstrates how to create a session receiver for a single, first available session.
     */
    @Test
    public void sessionReceiverSingleInstantiation() {
        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation#nextsession
        // The connectionString/queueName must be set by the application. The 'connectionString' format is shown below.
        // "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}"
        ServiceBusSessionReceiverAsyncClient sessionReceiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sessionReceiver()
            .queueName(queueName)
            .buildAsyncClient();

        // acceptNextSession() completes successfully with a receiver when it acquires the next available session.
        // `Flux.usingWhen` is used so we dispose of the receiver resource after `receiveMessages()` completes.
        // `Mono.usingWhen` can also be used if the resource closure only returns a single item.
        Flux<ServiceBusReceivedMessage> sessionMessages = Flux.usingWhen(
            sessionReceiver.acceptNextSession(),
            receiver -> receiver.receiveMessages(),
            receiver -> Mono.fromRunnable(() -> receiver.close()));

        // When program ends, or you're done receiving all messages, the `subscription` can be disposed of. This code
        // is non-blocking and kicks off the operation.
        Disposable subscription = sessionMessages.subscribe(
            message -> System.out.printf("Received Sequence #: %s. Contents: %s%n",
                message.getSequenceNumber(), message.getBody()),
            error -> System.err.print(error));
        // END: com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation#nextsession

        subscription.dispose();
        sessionReceiver.close();
    }

    /**
     * Demonstrates how to create a session receiver for a single know session id.
     */
    @Test
    public void sessionReceiverSessionIdInstantiation() {
        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation#sessionId
        // The connectionString/queueName must be set by the application. The 'connectionString' format is shown below.
        // "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}"
        ServiceBusSessionReceiverAsyncClient sessionReceiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sessionReceiver()
            .queueName(queueName)
            .buildAsyncClient();

        // acceptSession(String) completes successfully with a receiver when "<< my-session-id >>" session is
        // successfully locked.
        // `Flux.usingWhen` is used so we dispose of the receiver resource after `receiveMessages()` completes.
        // `Mono.usingWhen` can also be used if the resource closure only returns a single item.
        Flux<ServiceBusReceivedMessage> sessionMessages = Flux.usingWhen(
            sessionReceiver.acceptSession("<< my-session-id >>"),
            receiver -> receiver.receiveMessages(),
            receiver -> Mono.fromRunnable(() -> receiver.close()));

        // When program ends, or you're done receiving all messages, the `subscription` can be disposed of. This code
        // is non-blocking and kicks off the operation.
        Disposable subscription = sessionMessages.subscribe(
            message -> System.out.printf("Received Sequence #: %s. Contents: %s%n",
                message.getSequenceNumber(), message.getBody()),
            error -> System.err.print(error));
        // END: com.azure.messaging.servicebus.servicebusreceiverasyncclient.instantiation#sessionId

        subscription.dispose();
        sessionReceiver.close();
    }

    @Test
    public void transactionsSnippet() {
        // Some random sequenceNumber.
        long sequenceNumber = 1000L;
        ServiceBusReceivedMessage receivedMessage = new ServiceBusReceivedMessage(BinaryData.fromString("Hello"));

        // BEGIN: com.azure.messaging.servicebus.servicebusreceiverasyncclient.committransaction#servicebustransactioncontext
        // This mono creates a transaction and caches the output value, so we can associate operations with the
        // transaction. It does not cache the value if it is an error or completes with no items, effectively retrying
        // the operation.
        Mono<ServiceBusTransactionContext> transactionContext = receiver.createTransaction()
            .cache(value -> Duration.ofMillis(Long.MAX_VALUE),
                error -> Duration.ZERO,
                () -> Duration.ZERO);

        transactionContext.flatMap(transaction -> {
            // Process messages and associate operations with the transaction.
            Mono<Void> operations = Mono.when(
                receiver.receiveDeferredMessage(sequenceNumber).flatMap(message ->
                    receiver.complete(message, new CompleteOptions().setTransactionContext(transaction))),
                receiver.abandon(receivedMessage, new AbandonOptions().setTransactionContext(transaction)));

            // Finally, either commit or rollback the transaction once all the operations are associated with it.
            return operations.flatMap(transactionOperations -> receiver.commitTransaction(transaction));
        });
        // END: com.azure.messaging.servicebus.servicebusreceiverasyncclient.committransaction#servicebustransactioncontext

        receiver.close();
    }
}
