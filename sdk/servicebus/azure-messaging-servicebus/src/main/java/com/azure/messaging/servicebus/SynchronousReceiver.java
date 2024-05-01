// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.amqp.implementation.WindowedSubscriber;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusTracer;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import com.azure.core.amqp.implementation.WindowedSubscriber.WindowedSubscriberOptions;

import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.LOCK_TOKEN_KEY;
import static com.azure.messaging.servicebus.models.ServiceBusReceiveMode.PEEK_LOCK;

/**
 * A type that channels synchronous receive requests to a backing asynchronous receiver client.
 */
final class SynchronousReceiver {
    private static final String TERMINAL_MESSAGE;
    private static final WindowedSubscriber<ServiceBusReceivedMessage> DISPOSED;
    static {
        TERMINAL_MESSAGE = "The receiver client is terminated. Re-create the client to continue receive attempt.";
        DISPOSED = Flux.<ServiceBusReceivedMessage>error(new RuntimeException("Disposed."))
            .subscribeWith(new WindowedSubscriber<>(new HashMap<>(0), TERMINAL_MESSAGE, new WindowedSubscriberOptions<>()));
    }
    private static final String ENTITY_PATH_KEY = "entityPath";
    private static final String SYNC_RECEIVE_SPAN_NAME = "ServiceBus.receiveMessages";
    private static final Duration TIMEOUT_BETWEEN_MESSAGES = Duration.ofMillis(1000);
    private final ClientLogger logger;
    private final ServiceBusReceiverAsyncClient asyncClient;
    private final ServiceBusTracer tracer;
    private final AtomicReference<WindowedSubscriber<ServiceBusReceivedMessage>> subscriber = new AtomicReference<>(null);

    /**
     * Creates a SynchronousReceiver.
     *
     * @param logger the logger to use.
     * @param asyncClient the backing asynchronous client to connect to the broker, delegate message requesting and receive.
     */
    SynchronousReceiver(ClientLogger logger, ServiceBusReceiverAsyncClient asyncClient) {
        this.logger = Objects.requireNonNull(logger, "'logger' cannot be null.");
        this.asyncClient = Objects.requireNonNull(asyncClient, "'asyncClient' cannot be null.");
        this.tracer = asyncClient.getInstrumentation().getTracer();
    }

    /**
     * Request a specified number of messages and obtain an {@link IterableStream} streaming the received messages.
     *
     * @param maxMessages the maximum number of messages to receive.
     * @param maxWaitTime the upper bound for the time to wait to receive the requested number of messages.
     *
     * @return an {@link IterableStream} of at most {@code maxMessages} messages.
     */
    IterableStream<ServiceBusReceivedMessage> receive(int maxMessages, Duration maxWaitTime) {
        final WindowedSubscriber<ServiceBusReceivedMessage> s = subscriber.get();
        if (s != null) {
            return s.enqueueRequest(maxMessages, maxWaitTime);
        } else {
            return subscribeOnce().enqueueRequest(maxMessages, maxWaitTime);
        }
    }

    /**
     * Disposes the SynchronousReceiver.
     * <p>
     * Once disposed, the {@link IterableStream} for any future or pending receive requests will receive terminated error.
     * </p>
     */
    void dispose() {
        final WindowedSubscriber<ServiceBusReceivedMessage> s = subscriber.getAndSet(DISPOSED);
        if (s != null) {
            s.dispose();
        }
    }

    /**
     * Obtain a {@link WindowedSubscriber} that is subscribed to the asynchronous message stream produced by the backing
     * asynchronous client.
     * <p>
     * The method subscribes only once and cache the subscriber.
     * </p>
     * <p>
     * The subscriber exposes synchronous receive API and channels receive requests to the backing asynchronous message
     * stream.
     * </p>
     *
     * @return the subscriber to channel synchronous receive requests to the upstream.
     */
    private WindowedSubscriber<ServiceBusReceivedMessage> subscribeOnce() {
        if (!asyncClient.isV2()) {
            throw logger.logExceptionAsError(new UnsupportedOperationException("SynchronousReceiver requires v2 mode."));
        }
        final WindowedSubscriber<ServiceBusReceivedMessage> s = createSubscriber();
        if (subscriber.compareAndSet(null, s)) {
            // In case of concurrent invocation, the 's' created by the thread which lost the CAS race is eligible for GC.
            // There is no leak, as 's' is in mere constructed state and don’t own any leak-able resource yet.
            final Flux<ServiceBusReceivedMessage> upstream;
            if (asyncClient.isSessionEnabled()) {
                upstream = asyncClient.sessionSyncReceiveV2();
            } else {
                upstream = asyncClient.nonSessionSyncReceiveV2();
            }
            upstream.subscribeWith(s);
        }
        return subscriber.get();
    }

    /**
     * Create a {@link WindowedSubscriber} capable of bridging synchronous receive requests to an upstream of
     * asynchronous messages.
     *
     * @return The subscriber.
     */
    private WindowedSubscriber<ServiceBusReceivedMessage> createSubscriber() {
        final String entityPath = asyncClient.getEntityPath();
        final ReceiverOptions receiverOptions = asyncClient.getReceiverOptions();
        final boolean isPeekLockMode = receiverOptions.getReceiveMode() == PEEK_LOCK;
        final boolean isPrefetchDisabled = receiverOptions.getPrefetchCount() == 0;

        final WindowedSubscriberOptions<ServiceBusReceivedMessage> options = new WindowedSubscriberOptions<>();
        if (isPeekLockMode && isPrefetchDisabled) {
            options.setReleaser(this::messageReleaser);
        }
        options.setWindowDecorator(this::traceDecorator);
        options.setNextItemTimeout(TIMEOUT_BETWEEN_MESSAGES);

        return new WindowedSubscriber<>(Collections.singletonMap(ENTITY_PATH_KEY, entityPath), TERMINAL_MESSAGE, options);
    }

    /**
     * Releases the message back to the service.
     *
     * @param message the message to release.
     */
    private void messageReleaser(ServiceBusReceivedMessage message) {
        asyncClient.release(message)
            .subscribe(__ -> { },
                error -> logger.atWarning()
                    .addKeyValue(LOCK_TOKEN_KEY, message.getLockToken())
                    .log("couldn't release the message.", error),
                () -> logger.atVerbose()
                    .addKeyValue(LOCK_TOKEN_KEY, message.getLockToken())
                    .log("message successfully released."));
    }

    /**
     * Decorates the provided {@code toDecorate} flux for tracing the events (messages, termination) it produces.
     *
     * @param toDecorate the flux to decorate.
     * @return the flux decorated for tracing.
     */
    private Flux<ServiceBusReceivedMessage> traceDecorator(Flux<ServiceBusReceivedMessage> toDecorate) {
        final Flux<ServiceBusReceivedMessage> decorated = tracer.traceSyncReceive(SYNC_RECEIVE_SPAN_NAME, toDecorate);
        // TODO (anu) - discuss with Liudmila - do we need decorated.subscribe() here or IterableStream's internal
        //              subscription to the 'decorated' flux will do?
        decorated.subscribe();
        return decorated;
    }
}
