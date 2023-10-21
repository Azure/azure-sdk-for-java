package com.azure.android;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.azure.android.servicebus.PeekMessageAsync;
import com.azure.android.servicebus.SendMessageBatch;
import com.azure.android.servicebus.SendSessionMessageAsync;
import com.azure.android.servicebus.ServiceBusSessionProcessor;
import com.azure.core.util.BinaryData;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusMessageBatch;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import android.util.Log;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import reactor.core.Disposable;

@RunWith(AndroidJUnit4.class)
public class ServicebusSampleTests {

    private static final String RECEIVE_TAG = "ServiceBusReceiveMessageAndSettleAsyncOutput";
    private static final String SEND_TAG = "ServiceBusSendMessageOutput";

    final String serviceBusQueueName= "android-sb-queue";
    ClientSecretCredential clientSecretCredential;
    @Before
    public void setup() {
        // These are obtained by setting system environment variables
        // on the computer emulating the app
        clientSecretCredential = new ClientSecretCredentialBuilder()
            .clientId(BuildConfig.AZURE_CLIENT_ID)
            .clientSecret(BuildConfig.AZURE_CLIENT_SECRET)
            .tenantId(BuildConfig.AZURE_TENANT_ID)
            .build();
    }

    @Test
    public void peekMessageAsync() {
        try {
            PeekMessageAsync.main(serviceBusQueueName, clientSecretCredential);
        } catch (RuntimeException | InterruptedException e) {
            fail(e.getMessage());
        }
    }

    private static boolean processMessage(ServiceBusReceivedMessage message) {
        System.out.printf("Sequence #: %s. Contents: %s%n", message.getSequenceNumber(),
            message.getBody());

        return true;
    }

    @Test
    public void sendMessageBatch() {
        try {
            SendMessageBatch.main(serviceBusQueueName, clientSecretCredential);
        } catch (RuntimeException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void sendSessionMessageAsync() {
        try {
            SendSessionMessageAsync.main(serviceBusQueueName, clientSecretCredential);
        } catch (RuntimeException | InterruptedException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void serviceBusSessionProcessor() {
        try {
            ServiceBusSessionProcessor.main(serviceBusQueueName, clientSecretCredential);
        } catch (RuntimeException | InterruptedException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void receiveMessageAndSettleAsync() throws InterruptedException {
        AtomicBoolean sampleSuccessful = new AtomicBoolean(true);
        CountDownLatch countdownLatch = new CountDownLatch(1);


        // Create a receiver.
        // Messages are not automatically settled when `disableAutoComplete()` is toggled.
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .fullyQualifiedNamespace("android-service-bus.servicebus.windows.net")
            .credential(clientSecretCredential)
            .receiver()
            .queueName(serviceBusQueueName)
            .buildAsyncClient();

        assertNotNull(receiver);

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
                (ignore) -> Log.i(RECEIVE_TAG, "Message processed."),
                error -> sampleSuccessful.set(false)
            );

        // Subscribe is not a blocking call so we wait here so the program does not end.
        countdownLatch.await(10, TimeUnit.SECONDS);

        // Disposing of the subscription will cancel the receive() operation.
        subscription.dispose();

        // Close the receiver.
        receiver.close();

        // This assertion is to ensure that samples are working. Users should remove this.
        assertTrue(sampleSuccessful.get());
    }

    @Test
    public void sendMessage() throws InterruptedException {
        AtomicBoolean sampleSuccessful = new AtomicBoolean(false);
        CountDownLatch countdownLatch = new CountDownLatch(1);

        // Instantiate a client that will be used to call the service.
        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
            .fullyQualifiedNamespace("android-service-bus.servicebus.windows.net")
            .credential(clientSecretCredential)
            .sender()
            .queueName(serviceBusQueueName)
            .buildClient();

        assertNotNull(sender);

        // Create a message to send.
        final ServiceBusMessageBatch messageBatch = sender.createMessageBatch();
        IntStream.range(0, 10)
            .mapToObj(index -> new ServiceBusMessage(BinaryData.fromString("Hello world! " + index)))
            .forEach(message -> messageBatch.tryAddMessage(message));

        // Send that message. It completes successfully when the event has been delivered to the Service queue or topic.
        // It completes with an error if an exception occurred while sending the message.
        sender.sendMessages(messageBatch);
        assertEquals(messageBatch.getCount(), 10);

        // Close the sender.
        sender.close();
    }

}
