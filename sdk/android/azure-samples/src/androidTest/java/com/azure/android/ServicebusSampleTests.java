package com.azure.android;

import static com.azure.android.servicebus.ReceiveMessageProcessor.processMessage;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.azure.android.servicebus.ServiceBusSessionProcessor;
import com.azure.core.util.BinaryData;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusMessageBatch;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.models.CreateMessageBatchOptions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import reactor.core.Disposable;

@RunWith(AndroidJUnit4.class)
public class ServicebusSampleTests {

    private static final String PEEKMESSAGE_TAG = "ServiceBusPeekMessageAsyncOutput";
    private static final String SENDBATCH_TAG = "ServiceBusSendMessageBatchOutput";
    private static final String SENDSESSION_TAG = "ServiceBusSendSessionMessageAsyncOutput";
    private static final String PROCESSOR_TAG = "ServiceBusPeekMessageAsyncOutput";

    private static final String RECEIVE_TAG = "ServiceBusReceiveMessageAndSettleAsyncOutput";

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
    public void peekMessageAsync() throws InterruptedException {
        AtomicBoolean sampleSuccessful = new AtomicBoolean(false);
        CountDownLatch countdownLatch = new CountDownLatch(1);

        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .fullyQualifiedNamespace("android-service-bus.servicebus.windows.net")
            .credential(clientSecretCredential)
            .receiver()
            .queueName(serviceBusQueueName)
            .buildAsyncClient();

        assertFalse(receiver.getFullyQualifiedNamespace().isEmpty());

        receiver.peekMessage().subscribe(
            message -> {
                Log.i(PEEKMESSAGE_TAG, "Received Message Id: " + message.getMessageId());
                Log.i(PEEKMESSAGE_TAG, "Received Message: " + message.getBody());
            },
            error -> Log.e(PEEKMESSAGE_TAG, "Error occurred while receiving message: " + error),
            () -> {
                Log.i(PEEKMESSAGE_TAG, "Receiving complete.");
                sampleSuccessful.set(true);
            });

        // Subscribe is not a blocking call so we wait here so the program does not end while finishing
        // the peek operation.
        countdownLatch.await(10, TimeUnit.SECONDS);

        // Close the receiver.
        receiver.close();

        // If sampleSuccessful is false then fail the sample
        assertTrue(sampleSuccessful.get());

    }

    @Test
    public void sendMessageBatch() {
        List<ServiceBusMessage> testMessages = Arrays.asList(
            new ServiceBusMessage(BinaryData.fromString("Green")),
            new ServiceBusMessage(BinaryData.fromString("Red")),
            new ServiceBusMessage(BinaryData.fromString("Blue")),
            new ServiceBusMessage(BinaryData.fromString("Orange")));

        // Instantiate a client that will be used to call the service.
        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
            .fullyQualifiedNamespace("android-service-bus.servicebus.windows.net")
            .credential(clientSecretCredential)
            .sender()
            .queueName(serviceBusQueueName)
            .buildClient();

        assertFalse(sender.getFullyQualifiedNamespace().isEmpty());

        // Creates an ServiceBusMessageBatch where the ServiceBus.
        // If no maximumSizeInBatch is set, the maximum message size is used.
        ServiceBusMessageBatch currentBatch = sender.createMessageBatch(
            new CreateMessageBatchOptions().setMaximumSizeInBytes(1024));

        // We try to add as many messages as a batch can fit based on the maximum size and send to Service Bus when
        // the batch can hold no more messages. Create a new batch for next set of messages and repeat until all
        // messages are sent.
        for (ServiceBusMessage message : testMessages) {
            if (currentBatch.tryAddMessage(message)) {
                continue;
            }

            // The batch is full, so we create a new batch and send the batch.
            sender.sendMessages(currentBatch);
            currentBatch = sender.createMessageBatch();

            // Add that message that we couldn't before.
            if (!currentBatch.tryAddMessage(message)) {
                Log.e(SENDBATCH_TAG, String.format("Message is too large for an empty batch. Skipping. Max size: %s. Message: %s%n",
                    currentBatch.getMaxSizeInBytes(), message.getBody().toString()));
            }
        }

        sender.sendMessages(currentBatch);

        //close the client
        sender.close();
    }

    @Test
    public void sendSessionMessageAsync() throws InterruptedException {
        AtomicBoolean sampleSuccessful = new AtomicBoolean(false);
        CountDownLatch countdownLatch = new CountDownLatch(1);

        // We want all our greetings in the same session to be processed.
        String sessionId = "greetings-id";

        // Any clients built from the same ServiceBusClientBuilder share the same connection.
        ServiceBusClientBuilder builder = new ServiceBusClientBuilder()
            .fullyQualifiedNamespace("android-service-bus.servicebus.windows.net")
            .credential(clientSecretCredential);

        // Instantiate a client that will be used to send messages.
        ServiceBusSenderAsyncClient sender = builder
            .sender()
            .queueName(serviceBusQueueName)
            .buildAsyncClient();

        assertFalse(sender.getFullyQualifiedNamespace().isEmpty());

        // Setting the sessionId parameter ensures all messages end up in the same session and are received in order.
        List<ServiceBusMessage> messages = Arrays.asList(
            new ServiceBusMessage(BinaryData.fromBytes("Hello".getBytes(UTF_8))).setSessionId(sessionId),
            new ServiceBusMessage(BinaryData.fromBytes("Bonjour".getBytes(UTF_8))).setSessionId(sessionId)
        );

        // This sends all the messages in a single message batch.
        // This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
        // event has been delivered to the Service queue or topic. It completes with an error if an exception occurred
        // while sending the message.
        sender.sendMessages(messages).subscribe(unused -> Log.i(SENDSESSION_TAG, "Batch sent."),
            error -> Log.e(SENDSESSION_TAG, "Error occurred while publishing message batch: " + error),
            () -> {
                Log.i(SENDSESSION_TAG, "Batch send complete.");
                sampleSuccessful.set(true);
            });

        // subscribe() is not a blocking call. We wait here so the program does not end before the send is complete.
        countdownLatch.await(10, TimeUnit.SECONDS);

        // Close the sender.
        sender.close();

        // If sampleSuccessful is false then fail the sample
        assertTrue(sampleSuccessful.get());

    }

    @Test
    public void serviceBusSessionProcessor() throws InterruptedException {

        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
            .fullyQualifiedNamespace("android-service-bus.servicebus.windows.net")
            .credential(clientSecretCredential)
            .sessionProcessor()
            .queueName(serviceBusQueueName)
            .maxConcurrentSessions(2)
            .processMessage(ServiceBusSessionProcessor::processMessage)
            .processError(ServiceBusSessionProcessor::processError)
            .buildProcessorClient();

        assertNotNull(processorClient);

        Log.i(PROCESSOR_TAG, "Starting the processor");
        processorClient.start();

        TimeUnit.SECONDS.sleep(1);
        Log.i(PROCESSOR_TAG, "Stopping the processor");
        processorClient.stop();

        TimeUnit.SECONDS.sleep(1);
        Log.i(PROCESSOR_TAG, "Resuming the processor");
        processorClient.start();

        TimeUnit.SECONDS.sleep(1);
        Log.i(PROCESSOR_TAG, "Closing the processor");
        processorClient.close();
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
