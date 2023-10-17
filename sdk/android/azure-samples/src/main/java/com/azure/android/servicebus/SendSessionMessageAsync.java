// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.servicebus;

import com.azure.core.util.BinaryData;
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

    /**
     * Main method to invoke this demo on how to send and receive a {@link ServiceBusMessage} to and from a
     * session-enabled Azure Service Bus queue.
     *
     * @param connectionString
     * @param queueName
     * @throws InterruptedException If the program is unable to sleep while waiting for the operations to complete.
     */
    public static void main(String connectionString, String queueName) throws InterruptedException {
        SendSessionMessageAsync sample = new SendSessionMessageAsync();
        sample.run(connectionString, queueName);
    }

    /**
     * This method to invoke this demo on how to send and receive a {@link ServiceBusMessage} to and from a
     * session-enabled Azure Service Bus queue.
     *
     * @throws InterruptedException If the program is unable to sleep while waiting for the operations to complete.
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

        // We want all our greetings in the same session to be processed.
        String sessionId = "greetings-id";

        // Any clients built from the same ServiceBusClientBuilder share the same connection.
        ServiceBusClientBuilder builder = new ServiceBusClientBuilder()
            .connectionString(connectionString);

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
        sender.sendMessages(messages).subscribe(unused -> System.out.println("Batch sent."),
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
