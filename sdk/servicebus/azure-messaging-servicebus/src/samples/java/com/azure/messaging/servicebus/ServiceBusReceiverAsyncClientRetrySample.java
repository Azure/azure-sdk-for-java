// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * The sample demonstrates how handle terminal error from {@link ServiceBusReceiverAsyncClient} (aka LowLevelClient)
 * and recreate the LowLevelClient to continue receive.
 * 
 * <p>
 * The {@link ServiceBusReceiverAsyncClient} emits a terminal error (hence no longer emit messages) in
 * the following cases -
 *
 * <ul>
 * <li>When the connection encounters a non-retriable error. A few examples of non-retriable errors are - the app
 * attempting to connect to a queue that does not exist, someone deleting the queue in the middle of receiving,
 * the user explicitly initiating Geo-DR, user disabling the queue. These are certain events where the Service Bus
 * service communicates to the SDK that a non-retriable error occurred.
  * </li>
 * <li>a series of connection recovery attempts fail in a row which exhausts the max-retry.</li>
  * </ul>
 *
 * <p>
 * When these cases happen, the usual pattern is to log the terminal error for auditing and create a new client
 * to receive messages.
 */
public class ServiceBusReceiverAsyncClientRetrySample {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusReceiverAsyncClientRetrySample.class);

    String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
    String queueName = System.getenv("AZURE_SERVICEBUS_SAMPLE_QUEUE_NAME");

    /**
     * Main method to show how to handle terminal error from ServiceBusReceiverAsyncClient to
     * continue receive from Service Bus queue.
     *
     * @param args Unused arguments to the program.
     *
     * @throws InterruptedException if the program is unable to sleep while waiting for the receive.
     */
    public static void main(String[] args) throws InterruptedException {
        final ServiceBusReceiverAsyncClientRetrySample sample = new ServiceBusReceiverAsyncClientRetrySample();
        sample.run();
    }

    /**
     * Run method to invoke this demo on how to handle terminal error from ServiceBusReceiverAsyncClient to
     * continue receive from Service Bus queue.
     */
    @Test
    public void run() throws InterruptedException {
        final ServiceBusIndefiniteRetryReceiverAsyncClient client = new ServiceBusIndefiniteRetryReceiverAsyncClient(
            connectionString,
            queueName);
        handleMessages(client);
    }

    /**
     * A method uses provided client to receive messages, handle each message, then complete or abandon the message
     * depending on the message handling outcome.
     *
     * @param client The client to receive the message from.
     *
     * @throws InterruptedException if unable to sleep while waiting for the receive.
     */
    public void handleMessages(ServiceBusIndefiniteRetryReceiverAsyncClient client) throws InterruptedException {
        final AtomicBoolean sampleSuccessful = new AtomicBoolean(true);
        final CountDownLatch countdownLatch = new CountDownLatch(1);

        final Disposable subscription = client.receiveMessages()
            .flatMapSequential(new Function<ServiceBusReceivedMessage, Publisher<State>>() {
                @Override
                public Publisher<State> apply(ServiceBusReceivedMessage message) {
                    return handleMessage(message)
                        .onErrorResume(new Function<Throwable, Mono<State>>() {
                            @Override
                            public Mono<State> apply(Throwable businessError) {
                                try {
                                    // Note_1: The disposition (e.g., abandon, complete) are quick frame transfer
                                    // calls on already established amqp links. Here, the sample wait for disposition
                                    // ack. Alternatively, Mono.then() can be used for async chaining; in that case,
                                    // new credit will be placed asynchronously, and message can be buffered while
                                    // disposition ack is in transit.
                                    //
                                    // Note_2: In either case, it is important to handle any error from disposition
                                    // calls (here sample uses try-catch). Don't throw (explicitly or implicitly)
                                    // exceptions to SDK from the callback, as it will terminate the receive.
                                    client.abandon(message).block();
                                    return Mono.just(State.MESSAGE_ABANDONED);
                                } catch (Throwable abandonError) {
                                    // Logging exception here, not propagating, refer Note_2.
                                    LOGGER.warning("Couldn't abandon message {}", message.getMessageId(), abandonError);
                                    return Mono.just(State.MESSAGE_ABANDON_FAILED);
                                }
                            }
                        })
                        .flatMap(state -> {
                            if (state == State.HANDLING_SUCCEEDED) {
                                try {
                                    // Refer earlier Note_1 and Note_2.
                                    client.complete(message).block();
                                    return Mono.just(State.MESSAGE_COMPLETED);
                                } catch (Throwable completionError) {
                                    // Logging exception here, not propagating, refer Note_2.
                                    LOGGER.warning("Couldn't complete message {}", message.getMessageId(), completionError);
                                    return Mono.just(State.MESSAGE_COMPLETION_FAILED);
                                }
                            } else {
                                return Mono.just(state);
                            }
                        });
                }
            }, 1, 1)
            .then()
            .subscribe(__ -> { }, throwable -> sampleSuccessful.set(false));

        // Receiving messages from the queue for a duration of 20 seconds.
        // Subscribe is not a blocking call so we wait here so the program does not end.
        countdownLatch.await(20, TimeUnit.SECONDS);

        // Disposing of the subscription will cancel the receive operation.
        subscription.dispose();

        // Close the receiver.
        try {
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // This assertion is to ensure that samples are working. Users should remove this.
        Assertions.assertTrue(sampleSuccessful.get());
    }

    /**
     * A business domain specific logic taking 5 seconds to handle the message which randomly fails.
     *
     * @param message The message to handle.
     * @return a {@link Mono}  that signals once the message handling is completed.
     */
    private Mono<State> handleMessage(ServiceBusReceivedMessage message) {
        return Mono.fromCallable(() -> {
            try {
                // The sleep API is used only to demonstrate any external 'blocking' IO (e.g., network, DB) calls
                // that are part of message processing. For the demo, Mono.delay could  be also used rather than
                // sleep; we're keeping the sample simple, avoiding the thread switching from Mono.delay.
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 1;
        })
        .flatMap(ignored -> {
            LOGGER.info("Handling message: " + message.getMessageId());
            final boolean handlingSucceeded = Math.random() < 0.5;
            if (handlingSucceeded) {
                return Mono.just(State.HANDLING_SUCCEEDED);
            } else {
                return Mono.error(
                    new RuntimeException("Business logic failed to handle message: "
                        + message.getMessageId()));
            }
        });
    }

    /**
     * The business domain specific state of the message handling.
     */
    private enum State {
        HANDLING_SUCCEEDED,
        MESSAGE_COMPLETED,
        MESSAGE_ABANDONED,
        MESSAGE_COMPLETION_FAILED,
        MESSAGE_ABANDON_FAILED
    }

    /**
     * A facade receiver client that uses {@link ServiceBusReceiverAsyncClient} underneath, stream messages from it,
     * but additionally listen for terminal error and create a new {@link ServiceBusReceiverAsyncClient}
     * to continue the message delivery.
     */
    private static final class ServiceBusIndefiniteRetryReceiverAsyncClient implements AutoCloseable {
        private static final ClientLogger LOGGER = new ClientLogger(ServiceBusIndefiniteRetryReceiverAsyncClient.class);
        // On rare cases when Retry exhausts or a non-retryable error occurs do a fixed back-off for 4 sec.
        private static final Duration RETRY_WAIT_TIME = Duration.ofSeconds(4);

        private final String connectionString;
        private final String queueName;
        private final AtomicReference<ServiceBusReceiverAsyncClient> currentLowLevelClient = new AtomicReference<>();
        private final AtomicBoolean isClosed = new AtomicBoolean(false);
        private final AtomicBoolean isInitial = new AtomicBoolean(true);

        /**
         * Creates an instance of ServiceBusIndefiniteRetryReceiverAsyncClient.
         *
         * @param connectionString The Service Bus connection string.
         * @param queueName The Service Bus queue name.
         */
        ServiceBusIndefiniteRetryReceiverAsyncClient(String connectionString,
                                                            String queueName) {
            this.connectionString = connectionString;
            this.queueName = queueName;
            this.currentLowLevelClient.set(createLowLevelClient());
        }

        /**
         * Receive messages from the Service Bus queue.
         *
         * @return a {@link Flux} that streams messages from the Service Bus queue, transparently retrying if
         * the underlying {@link ServiceBusReceiverAsyncClient} terminate with error.
         */
        Flux<ServiceBusReceivedMessage> receiveMessages() {
            return Flux.using(
                    () -> {
                        if (isClosed.get()) {
                            throw new IllegalStateException("Cannot perform receive on the closed client.");
                        }
                        if (!isInitial.getAndSet(false)) {
                            LOGGER.verbose("Creating a new LowLevelClient");
                            currentLowLevelClient.set(createLowLevelClient());
                        }
                        return currentLowLevelClient.get();
                    },
                    client ->  {
                        return client.receiveMessages();
                    },
                    client -> {
                        LOGGER.verbose("Disposing current LowLevelClient");
                        client.close();
                    })
                .retryWhen(
                    Retry.fixedDelay(Long.MAX_VALUE, RETRY_WAIT_TIME)
                        .filter(throwable -> {
                            if (isClosed.get()) {
                                return false;
                            }
                            LOGGER.warning("Current LowLevelClient's retry exhausted or a non-retryable error occurred.",
                                throwable);
                            return true;
                        }));
        }

        /**
         * Completes a {@link ServiceBusReceivedMessage message}. This will delete the message from the service.
         *
         * @param message the {@link ServiceBusReceivedMessage} to perform this operation.
         * @return a {@link Mono} that finishes when the message is completed on Service Bus.
         */
        Mono<Void> complete(ServiceBusReceivedMessage message) {
            final ServiceBusReceiverAsyncClient lowLevelClient = currentLowLevelClient.get();
            return lowLevelClient.complete(message);

        }

        /**
         * Abandons a {@link ServiceBusReceivedMessage message}. This will make the message available again for processing.
         * Abandoning a message will increase the delivery count on the message.
         *
         * @param message the {@link ServiceBusReceivedMessage} to perform this operation.
         * @return a {@link Mono} that completes when the Service Bus abandon operation completes.
         */
        Mono<Void> abandon(ServiceBusReceivedMessage message) {
            final ServiceBusReceiverAsyncClient lowLevelClient = currentLowLevelClient.get();
            return lowLevelClient.abandon(message);
        }

        /**
         * Disposes of the client by closing the underlying {@link ServiceBusReceiverAsyncClient}.
         */
        @Override
        public void close() {
            if (!isClosed.getAndSet(true)) {
                this.currentLowLevelClient.get().close();
            }
        }

        /**
         * Create a new {@link ServiceBusReceiverAsyncClient} to receive messages from queue.
         *
         * @return the {@link ServiceBusReceiverAsyncClient}.
         */
        private ServiceBusReceiverAsyncClient createLowLevelClient() {
            return new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .receiver()
                .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                .queueName(queueName)
                .disableAutoComplete()
                .maxAutoLockRenewDuration(Duration.ZERO)
                .prefetchCount(0)
                .buildAsyncClient();
        }
    }
}
