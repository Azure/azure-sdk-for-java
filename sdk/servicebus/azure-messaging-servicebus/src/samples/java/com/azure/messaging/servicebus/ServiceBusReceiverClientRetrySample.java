// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The sample demonstrates how handle terminal error when enumerating the {@link com.azure.core.util.IterableStream} from
 * {@link ServiceBusReceiverClient#receiveMessages(int, Duration)} or {@link ServiceBusReceiverClient#receiveMessages(int)}
 * and recreate the client to continue receiving messages.
 *
 * <p>
 * Enumerating the {@link com.azure.core.util.IterableStream} from the receiveMessages APIs may emit a terminal error
 * (hence no longer emit messages) in the following cases -
 *
 * <ul>
 * <li>When the connection or link encounters a non-retriable error. A few examples of non-retriable errors are -
 * the app attempting to connect to a queue that does not exist, someone deleting the queue in the middle of receiving,
 * the user explicitly initiating Geo-DR, user disabling the queue. These are certain events where the Service Bus
 * service communicates to the SDK that a non-retriable error occurred.
  * </li>
 * <li>a series of connection or link recovery attempts fail in a row which exhausts the max-retry.</li>
  * </ul>
 *
 * <p>
 * When these cases happen, the usual pattern is to log the terminal error for auditing, close current client and create
 * a new client to continue receiving messages.
 */
public class ServiceBusReceiverClientRetrySample {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusReceiverClientRetrySample.class);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
    String queueName = System.getenv("AZURE_SERVICEBUS_SAMPLE_QUEUE_NAME");

    /**
     * Main method of the sample showing how to handle terminal error that {@link com.azure.core.util.IterableStream}
     * from receiveMessages API throws and recreates the client to continue receiving messages.
     *
     * @param args Unused arguments to the program.
     * @throws InterruptedException if the program is unable to sleep while waiting for the receive.
     */
    public static void main(String[] args) throws InterruptedException {
        final ServiceBusReceiverClientRetrySample sample = new ServiceBusReceiverClientRetrySample();
        sample.run();
    }

    /**
     * Run method to invoke this demo on how to handle terminal error that {@link com.azure.core.util.IterableStream}
     * from receiveMessages API throws and recreates the client to continue receiving messages.
     */
    @Test
    public void run() throws InterruptedException {
        startReceive();
    }

    void startReceive() {
        isRunning.set(true);
        receiveMessages();
    }

    void stopReceive() {
        isRunning.set(false);
    }

    private void receiveMessages() {
        final ServiceBusReceiverClient client = createClient();

        final int maxMessages = 5;
        final Duration maxWaitTime = Duration.ofSeconds(30);

        Exception terminalError = null;
        // The message loop
        while (isRunning.get()) {
            final IterableStream<ServiceBusReceivedMessage> messages = client.receiveMessages(maxMessages, maxWaitTime);
            try {
                messages.forEach(message -> {
                    final boolean success = handleMessage(message);
                    if (success) {
                        completeMessage(client, message);
                    } else {
                        abandonMessage(client, message);
                    }
                });
            } catch (Exception enumerationError) {
                terminalError = enumerationError;
                // Handle the terminal error while enumerating the messages. The 'messages' enumeration (forEach) can
                // throw if the client runs into a non-retriable error or retry exhausts. In such cases, the current
                // client reaches terminal state and can no longer receive, so exit the message loop,
                break;
            }
        }
        // close the client
        client.close();
        if (!isRunning.get()) {
            return;
        }
        // log the terminal error for auditing
        LOGGER.warning("Receiver client's retry exhausted or a non-retryable error occurred.", terminalError);
        // and attempt to receive using new client.
        receiveMessages();
    }

    /**
     * A business domain specific logic taking 5 seconds to handle the message which randomly fails.
     *
     * @param message The message to handle.
     * @return {@code true} if message handled successfully, {@code false} otherwise.
     */
    private boolean handleMessage(ServiceBusReceivedMessage message) {
        LOGGER.info("Handling message: " + message.getMessageId());
        try {
            // The sleep API is used only to demonstrate any external 'blocking' IO (e.g., network, DB)
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final boolean handlingSucceeded = Math.random() < 0.5;
        if (handlingSucceeded) {
            return true;
        } else {
            LOGGER.info("Business logic failed to handle message: " + message.getMessageId());
            return false;
        }
    }

    /**
     * Completes the message using the given client and logs any exception during completion.
     *
     * @param client the client.
     * @param message the message to complete.
     */
    private void completeMessage(ServiceBusReceiverClient client, ServiceBusReceivedMessage message) {
        try {
            client.complete(message);
        } catch (Throwable completionError) {
            LOGGER.warning("Couldn't complete message {}", message.getMessageId(), completionError);
        }
    }

    /**
     * Abandon the message using the given client and logs any exception during abandoning.
     *
     * @param client the client.
     * @param message the message to complete.
     */
    private void abandonMessage(ServiceBusReceiverClient client, ServiceBusReceivedMessage message) {
        try {
            client.abandon(message);
        } catch (Throwable abandonError) {
            LOGGER.warning("Couldn't abandon message {}", message.getMessageId(), abandonError);
        }
    }

    /**
     * Creates a receiver client.
     *
     * @return the receiver client.
     */
    private ServiceBusReceiverClient createClient() {
        return new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .queueName(queueName)
            .disableAutoComplete()
            .maxAutoLockRenewDuration(Duration.ZERO)
            .prefetchCount(0)
            .buildClient();
    }
}
