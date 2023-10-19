// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.servicebus;

import com.azure.identity.ClientSecretCredential;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;

import android.util.Log;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Sample example showing how peek would work.
 */
public class PeekMessageAsync {

    private static final String TAG = "ServiceBusPeekMessageAsyncOutput";


    public static void main(String queueName, ClientSecretCredential credential) throws InterruptedException {
        AtomicBoolean sampleSuccessful = new AtomicBoolean(false);
        CountDownLatch countdownLatch = new CountDownLatch(1);

        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .fullyQualifiedNamespace("android-service-bus.servicebus.windows.net")
            .credential(credential)
            .receiver()
            .queueName(queueName)
            .buildAsyncClient();

        receiver.peekMessage().subscribe(
            message -> {
                Log.i(TAG, "Received Message Id: " + message.getMessageId());
                Log.i(TAG, "Received Message: " + message.getBody());
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

    }
}
