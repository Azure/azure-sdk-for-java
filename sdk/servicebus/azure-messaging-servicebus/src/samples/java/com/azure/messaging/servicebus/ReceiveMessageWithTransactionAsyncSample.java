// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ReceiveMode;
import reactor.core.Disposable;
import reactor.core.publisher.SignalType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ReceiveMessageWithTransactionAsyncSample {

    /**
     * Main method to invoke this demo on how to use transaction API and settle the {@link ServiceBusReceivedMessage}
     * as part of a transaction.
     *
     * @param args Unused arguments to the program.
     * @throws InterruptedException If the program is unable to sleep while waiting for the operations to complete.
     */
    public static void main(String[] args) throws InterruptedException {

        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Go to "Shared access policies"
        // 3. Copy the connection string for the "RootManageSharedAccessKey" policy.
        String connectionString = "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};"
            + "SharedAccessKey={key}";

        // Create a receiver.
        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<queue-name>>" will be the name of the Service Bus queue instance you created
        // inside the Service Bus namespace.
        ServiceBusClientBuilder builder = new ServiceBusClientBuilder()
            .connectionString(connectionString);

        // At most, the receiver will automatically renew the message lock until 120 seconds have elapsed.
        // By default, after messages are processed, the onus is on users to complete, abandon, defer, or dead-letter the
        // message when they are finished with it.

        ServiceBusReceiverAsyncClient receiver = builder
            .receiver()
            .receiveMode(ReceiveMode.PEEK_LOCK)
            .queueName("<<queue-name>>")
            .maxAutoLockRenewalDuration(Duration.ofSeconds(120))
            .buildAsyncClient();

        ServiceBusSenderAsyncClient sender = builder.sender()
            .queueName("<<queue-name>>")
            .buildAsyncClient();

        List<ServiceBusMessage> messages = new ArrayList<>();
        messages.add(new ServiceBusMessage("Hello world!".getBytes(UTF_8)));
        messages.add(new ServiceBusMessage("Hello world!".getBytes(UTF_8)));

        // Send some messages.
        sender.send(messages).block();

        // Hold the transaction.
        AtomicReference<ServiceBusTransactionContext> transaction = new AtomicReference<>();

        // Transaction is actually started in ServiceBus until you perform first operation
        // (Example receiver.complete(message, transaction)) with it.
        // Create transaction and start receiving messages
        Disposable subscription = receiver.createTransaction()
            .flatMapMany(transactionContext -> {
                transaction.set(transactionContext);
                return receiver.receive();
            })
            .take(2)
            .flatMap(context -> {
                boolean messageProcessed = true;
                // Process the context and its message here.
                // Change the `messageProcessed` according to you business logic and if you are able to process the
                // message successfully.

                if (messageProcessed) {
                    return receiver.complete(context.getMessage(), transaction.get());
                } else {
                    return receiver.abandon(context.getMessage(), null, transaction.get());
                }
            })
            .doFinally(signal -> {
                if (signal == SignalType.ON_COMPLETE) {
                    receiver.commitTransaction(transaction.get()).subscribe();
                } else {
                    receiver.rollbackTransaction(transaction.get()).subscribe();
                }
            })
            .subscribe();

        // Subscribe is not a blocking call so we sleep here so the program does not end.
        TimeUnit.SECONDS.sleep(30);

        // Disposing of the subscription will cancel the receive() operation.
        subscription.dispose();

        // Close the clients.
        receiver.close();
        sender.close();
    }
}
