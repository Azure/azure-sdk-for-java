// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Sample demonstrates how to schedule a {@link ServiceBusMessage} to an Azure Service Bus queue and cancel a scheduled
 * message.
 */
public class SendScheduledMessageAndCancelAsyncSample {
    String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
    String queueName = System.getenv("AZURE_SERVICEBUS_SAMPLE_QUEUE_NAME");

    /**
     * Main method to invoke this demo on how to schedule and then cancel a message to an Azure Service Bus queue.
     *
     * @param args Unused arguments to the program.
     * @throws InterruptedException If the program is unable to sleep while waiting for the operations to complete.
     */
    public static void main(String[] args) throws InterruptedException {
        SendScheduledMessageAndCancelAsyncSample sample = new SendScheduledMessageAndCancelAsyncSample();
        sample.run();
    }

    /**
     * This method to invoke this demo on how to schedule and then cancel a message to an Azure Service Bus queue.
     *
     * @throws InterruptedException If the program is unable to sleep while waiting for the operations to complete.
     */
    @Test
    public void run() throws InterruptedException {
        AtomicBoolean sampleSuccessful = new AtomicBoolean(false);
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

        // Instantiate a client that will be used to call the service.
        ServiceBusSenderAsyncClient sender = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .queueName(queueName)
            .buildAsyncClient();

        ServiceBusMessage message = new ServiceBusMessage(BinaryData.fromString("Hello World!!"));
        AtomicLong messageSequenceNumber = new AtomicLong();
        Semaphore completedSemaphore = new Semaphore(1);
        completedSemaphore.acquire();

        // Scheduling the message to appear in the queue one minute from now.
        // Following call returns a Mono<Void>, which we subscribe to. It completes successfully when the message has
        // been scheduled. It completes with an error if an exception occurs while scheduling the message.
        sender.scheduleMessage(message, OffsetDateTime.now().plusSeconds(60))
            .subscribe(sequenceNumber -> {
                System.out.printf("Sequence number of scheduled message: %s%n", sequenceNumber);
                messageSequenceNumber.set(sequenceNumber);
            },
                error -> {
                    System.err.println("Error occurred while scheduling message. " + error);
                    completedSemaphore.release();
                }, () -> {
                    System.out.println("Completed scheduling message.");
                    completedSemaphore.release();
                    sampleSuccessful.set(true);
                });

        // Waiting until the scheduling operation completes so we can move on.
        if (!completedSemaphore.tryAcquire(20, TimeUnit.SECONDS)) {
            System.err.println("Unable to acquire semaphore because message was not scheduled yet.");
            return;
        }

        // Cancel the message we had scheduled.
        System.out.println("Cancelling scheduled message with sequence number: " + messageSequenceNumber.get());
        sender.cancelScheduledMessage(messageSequenceNumber.get())
            .subscribe(
                unused -> System.out.println("Cancelled message."),
                error -> System.err.println("Error occurred while cancelling message. " + error),
                () -> System.out.println("Completed cancelling message."));

        // Subscribe is not a blocking call so we wait here so the program does not end while finishing
        // the operation.
        countdownLatch.await(5, TimeUnit.SECONDS);

        // Dispose of the sender and any resources it holds.
        sender.close();

        // This assertion is to ensure that samples are working. Users should remove this.
        Assertions.assertTrue(sampleSuccessful.get());
    }
}
