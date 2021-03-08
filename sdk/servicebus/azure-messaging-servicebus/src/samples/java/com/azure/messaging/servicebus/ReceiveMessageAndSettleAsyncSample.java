// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Sample demonstrates how to receive an {@link ServiceBusReceivedMessage} from an Azure Service Bus Queue and settle
 * it <b>manually</b>.
 *
 * Messages can be settled via:
 * <ul>
 * <li>{@link ServiceBusReceiverAsyncClient#complete(ServiceBusReceivedMessage) complete}</li>
 * <li>{@link ServiceBusReceiverAsyncClient#defer(ServiceBusReceivedMessage) defer}</li>
 * <li>{@link ServiceBusReceiverAsyncClient#abandon(ServiceBusReceivedMessage) abandon}</li>
 * <li>{@link ServiceBusReceiverAsyncClient#deadLetter(ServiceBusReceivedMessage) dead-letter}</li>
 * </ul>
 *
 */
public class ReceiveMessageAndSettleAsyncSample {
    String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
    String queueName = System.getenv("AZURE_SERVICEBUS_SAMPLE_QUEUE_NAME");

    /**
     * Main method to invoke this demo on how to receive an {@link ServiceBusReceivedMessage} from an Azure Service Bus
     * Queue
     *
     * @param args Unused arguments to the program.
     * @throws InterruptedException If the program is unable to sleep while waiting for the operations to complete.
     */
    public static void main(String[] args) throws InterruptedException {
        ReceiveMessageAndSettleAsyncSample sample = new ReceiveMessageAndSettleAsyncSample();
        sample.run();
    }

    /**
     * This method to invoke this demo on how to receive an {@link ServiceBusReceivedMessage} from an Azure Service Bus
     * Queue.
     *
     * @throws InterruptedException If the program is unable to sleep while waiting for the receive to complete.
     */
    @Test
    public void run() throws InterruptedException {
        AtomicBoolean sampleSuccessful = new AtomicBoolean(true);
        CountDownLatch countdownLatch = new CountDownLatch(1);

        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Go to "Shared access policies"
        // 3. Copy the connection string for the "RootManageSharedAccessKey" policy.
        // The 'connectionString' format is shown below.
        // 1. "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}"
        // 2. "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // 3. "queueName" will be the name of the Service Bus queue instance you created
        //    inside the Service Bus namespace.

        // Create a receiver.
        // Messages are not automatically settled when `disableAutoComplete()` is toggled.
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .disableAutoComplete()
            .queueName(queueName)
            .buildAsyncClient();

        Disposable subscription = receiver.receiveMessages()
            .flatMap(message -> {
                boolean messageProcessed = processMessage(message);

                // Process the context and its message here.
                // Change the `messageProcessed` according to you business logic and if you are able to process the
                // message successfully.
                // Messages MUST be manually settled because automatic settlement was disabled when creating the
                // receiver.
                if (messageProcessed) {
                    return receiver.complete(message);
                } else {
                    return receiver.abandon(message);
                }
            }).subscribe(
                (ignore) -> System.out.println("Message processed."),
                error -> sampleSuccessful.set(false)
            );

        // Subscribe is not a blocking call so we wait here so the program does not end.
        countdownLatch.await(10, TimeUnit.SECONDS);

        // Disposing of the subscription will cancel the receive() operation.
        subscription.dispose();

        // Close the receiver.
        receiver.close();

        // This assertion is to ensure that samples are working. Users should remove this.
        Assertions.assertTrue(sampleSuccessful.get());
    }

    private static boolean processMessage(ServiceBusReceivedMessage message) {
        System.out.printf("Sequence #: %s. Contents: %s%n", message.getSequenceNumber(),
            message.getBody());

        return true;
    }
}
