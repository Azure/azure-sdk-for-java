// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.servicebus;

import com.azure.core.util.BinaryData;
import com.azure.identity.ClientSecretCredential;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SendSessionMessageAsync {

    private static final String TAG = "SendSessionMessageAsyncOutput";


    public static void main(String queueName, ClientSecretCredential credential) throws InterruptedException {
        AtomicBoolean sampleSuccessful = new AtomicBoolean(false);
        CountDownLatch countdownLatch = new CountDownLatch(1);

        // We want all our greetings in the same session to be processed.
        String sessionId = "greetings-id";

        // Any clients built from the same ServiceBusClientBuilder share the same connection.
        ServiceBusClientBuilder builder = new ServiceBusClientBuilder()
            .fullyQualifiedNamespace("https://android-service-bus.servicebus.windows.net")
            .credential(credential);

        // Instantiate a client that will be used to send messages.
        ServiceBusSenderAsyncClient sender = builder
            .sender()
            .queueName(queueName)
            .buildAsyncClient();

        // Setting the sessionId parameter ensures all messages end up in the same session and are received in order.
        List<ServiceBusMessage> messages = Arrays.asList(
            new ServiceBusMessage(BinaryData.fromBytes("Hello".getBytes(UTF_8))).setSessionId(sessionId),
            new ServiceBusMessage(BinaryData.fromBytes("Bonjour".getBytes(UTF_8))).setSessionId(sessionId)
        );

        // This sends all the messages in a single message batch.
        // This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
        // event has been delivered to the Service queue or topic. It completes with an error if an exception occurred
        // while sending the message.
        sender.sendMessages(messages).subscribe(unused -> Log.i(TAG, "Batch sent."),
            error -> Log.e(TAG, "Error occurred while publishing message batch: " + error),
            () -> {
                Log.i(TAG, "Batch send complete.");
                sampleSuccessful.set(true);
            });

        // subscribe() is not a blocking call. We wait here so the program does not end before the send is complete.
        countdownLatch.await(10, TimeUnit.SECONDS);

        // Close the sender.
        sender.close();

        // This assertion is to ensure samples are working. Users should remove this.
        Assertions.assertTrue(sampleSuccessful.get());
    }
}
