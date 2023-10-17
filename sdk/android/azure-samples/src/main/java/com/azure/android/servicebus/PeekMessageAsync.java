// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import android.util.Log;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Sample example showing how peek would work.
 */
public class PeekMessageAsync {

    private static final String TAG = "PeekMessageAsyncOutput";

    /**
     * Main method to invoke this demo on how to peek at a message within a Service Bus Queue.
     *
     * @param connectionString
     * @param queueName
     * @throws InterruptedException If the program is unable to sleep while waiting for the receive to complete.
     */
    public static void main(String connectionString, String queueName) throws InterruptedException {
        PeekMessageAsync sample = new PeekMessageAsync();
        sample.run(connectionString, queueName);
    }

    /**
     * run method to invoke this demo on how to peek at a message within a Service Bus Queue.
     *
     * @throws InterruptedException If the program is unable to sleep while waiting for the receive to complete.
     */
    @Test
    public void run(String connectionString, String queueName) throws InterruptedException {
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

        // Create a receiver using connection string.
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .queueName(queueName)
            .buildAsyncClient();

        receiver.peekMessage().subscribe(
            message -> {
                Log.i(TAG, "Received Message Id: " + message.getMessageId());
                Log.i(TAG, "Received Message: " + message.getBody().toString());
            },
            error -> Log.e(TAG, "Error occurred while receiving message: " + error),
            () -> {
                Log.i(TAG, "Receiving complete.");
                sampleSuccessful.set(true);
            });

        // Subscribe is not a blocking call so we wait here so the program does not end while finishing
        // the peek operation.
        countdownLatch.await(10, TimeUnit.SECONDS);

        // Close the receiver.
        receiver.close();

        // This assertion is to ensure that samples are working. Users should remove this.
        Assertions.assertTrue(sampleSuccessful.get());
    }
}
