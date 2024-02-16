// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.AmqpErrorCode;
import com.azure.core.amqp.implementation.ReactorDispatcher;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.messaging.Released;
import org.apache.qpid.proton.amqp.transaction.Declared;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Delivery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReceiverUnsettledDeliveriesTest {
    private static final UUID DELIVERY_EMPTY_TAG = new UUID(0L, 0L);
    private static final String HOSTNAME = "hostname";
    private static final String ENTITY_PATH = "/orders";
    private static final String RECEIVER_LINK_NAME = "orders-link";
    private static final String DISPOSITION_ERROR_ON_CLOSE
        = "The receiver didn't receive the disposition " + "acknowledgment due to receive link closure.";
    private static final ClientLogger LOGGER = new ClientLogger(ReceiverUnsettledDeliveriesTest.class);
    private static final AmqpRetryOptions RETRY_OPTIONS = new AmqpRetryOptions();
    private AutoCloseable mocksCloseable;
    @Mock
    private ReactorDispatcher reactorDispatcher;
    @Mock
    private Delivery delivery;

    @BeforeEach
    public void setup() throws IOException {
        mocksCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void teardown() throws Exception {
        Mockito.framework().clearInlineMock(this);

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @Test
    public void tracksOnDelivery() throws IOException {
        doNothing().when(reactorDispatcher).invoke(any(Runnable.class));

        try (ReceiverUnsettledDeliveries deliveries = createUnsettledDeliveries()) {
            final UUID deliveryTag = UUID.randomUUID();
            deliveries.onDelivery(deliveryTag, delivery);
            assertTrue(deliveries.containsDelivery(deliveryTag));
        }
    }

    @Test
    public void sendDispositionEmitsDeliveryNotOnLinkExceptionForUntrackedDelivery() {
        try (ReceiverUnsettledDeliveries deliveries = createUnsettledDeliveries()) {
            final UUID deliveryTag = UUID.randomUUID();
            final Mono<Void> dispositionMono
                = deliveries.sendDisposition(deliveryTag.toString(), Accepted.getInstance());
            StepVerifier.create(dispositionMono).verifyError(DeliveryNotOnLinkException.class);
        }
    }

    @Test
    public void sendDispositionEmitsDeliveryNotOnLinkExceptionIfClosed() {
        ReceiverUnsettledDeliveries deliveries = createUnsettledDeliveries();
        deliveries.close();
        final UUID deliveryTag = UUID.randomUUID();
        final Mono<Void> dispositionMono = deliveries.sendDisposition(deliveryTag.toString(), Accepted.getInstance());
        StepVerifier.create(dispositionMono).verifyError(DeliveryNotOnLinkException.class);
    }

    @Test
    public void sendDispositionErrorsOnDispatcherIOException() throws IOException {
        final UUID deliveryTag = UUID.randomUUID();

        doThrow(new IOException()).when(reactorDispatcher).invoke(any(Runnable.class));

        try (ReceiverUnsettledDeliveries deliveries = createUnsettledDeliveries()) {
            deliveries.onDelivery(deliveryTag, delivery);
            final Mono<Void> dispositionMono
                = deliveries.sendDisposition(deliveryTag.toString(), Accepted.getInstance());
            StepVerifier.create(dispositionMono).verifyErrorSatisfies(error -> {
                Assertions.assertInstanceOf(AmqpException.class, error);
                Assertions.assertNotNull(error.getCause());
                Assertions.assertInstanceOf(IOException.class, error.getCause());
            });
        }
    }

    @Test
    public void sendDispositionErrorsOnDispatcherRejectedException() throws IOException {
        final UUID deliveryTag = UUID.randomUUID();

        doThrow(new RejectedExecutionException()).when(reactorDispatcher).invoke(any(Runnable.class));

        try (ReceiverUnsettledDeliveries deliveries = createUnsettledDeliveries()) {
            deliveries.onDelivery(deliveryTag, delivery);
            final Mono<Void> dispositionMono
                = deliveries.sendDisposition(deliveryTag.toString(), Accepted.getInstance());
            StepVerifier.create(dispositionMono).verifyErrorSatisfies(error -> {
                Assertions.assertInstanceOf(AmqpException.class, error);
                Assertions.assertNotNull(error.getCause());
                Assertions.assertInstanceOf(RejectedExecutionException.class, error.getCause());
            });
        }
    }

    @Test
    public void sendDispositionErrorsIfSameDeliveryDispositionInProgress() throws IOException {
        final UUID deliveryTag = UUID.randomUUID();
        final DeliveryState desiredState = Accepted.getInstance();

        doAnswer(byRunningRunnable()).when(reactorDispatcher).invoke(any(Runnable.class));

        try (ReceiverUnsettledDeliveries deliveries = createUnsettledDeliveries()) {
            deliveries.onDelivery(deliveryTag, delivery);
            final Mono<Void> dispositionMono1 = deliveries.sendDisposition(deliveryTag.toString(), desiredState);
            dispositionMono1.subscribe();
            final Mono<Void> dispositionMono2 = deliveries.sendDisposition(deliveryTag.toString(), desiredState);
            StepVerifier.create(dispositionMono2).verifyError(AmqpException.class);
        }
    }

    @Test
    public void sendDispositionCompletesOnSuccessfulOutcome() throws IOException {
        final UUID deliveryTag = UUID.randomUUID();
        final DeliveryState desiredState = Accepted.getInstance();
        final DeliveryState remoteState = desiredState;

        doAnswer(byRunningRunnable()).when(reactorDispatcher).invoke(any(Runnable.class));
        when(delivery.getRemoteState()).thenReturn(remoteState);
        when(delivery.remotelySettled()).thenReturn(true);

        try (ReceiverUnsettledDeliveries deliveries = createUnsettledDeliveries()) {
            deliveries.onDelivery(deliveryTag, delivery);
            final Mono<Void> dispositionMono = deliveries.sendDisposition(deliveryTag.toString(), desiredState);
            StepVerifier.create(dispositionMono)
                .then(() -> deliveries.onDispositionAck(deliveryTag, delivery))
                .verifyComplete();
            verify(delivery).disposition(desiredState);
            Assertions.assertFalse(deliveries.containsDelivery(deliveryTag));
        }
    }

    @Test
    public void sendDispositionErrorsOnReleaseOutcome() throws IOException {
        final UUID deliveryTag = UUID.randomUUID();
        final DeliveryState desiredState = Accepted.getInstance();
        final DeliveryState remoteState = Released.getInstance();

        doAnswer(byRunningRunnable()).when(reactorDispatcher).invoke(any(Runnable.class));
        when(delivery.getRemoteState()).thenReturn(remoteState);
        when(delivery.remotelySettled()).thenReturn(true);

        try (ReceiverUnsettledDeliveries deliveries = createUnsettledDeliveries()) {
            deliveries.onDelivery(deliveryTag, delivery);
            final Mono<Void> dispositionMono = deliveries.sendDisposition(deliveryTag.toString(), desiredState);
            StepVerifier.create(dispositionMono)
                .then(() -> deliveries.onDispositionAck(deliveryTag, delivery))
                .verifyErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(AmqpException.class, error);
                    final AmqpException amqpError = (AmqpException) error;
                    Assertions.assertNotNull(amqpError.getErrorCondition());
                    Assertions.assertEquals(AmqpErrorCondition.OPERATION_CANCELLED, amqpError.getErrorCondition());
                });
            verify(delivery).disposition(desiredState);
            Assertions.assertFalse(deliveries.containsDelivery(deliveryTag));
        }
    }

    @Test
    public void sendDispositionErrorsOnUnknownOutcome() throws IOException {
        final UUID deliveryTag = UUID.randomUUID();
        final DeliveryState desiredState = Accepted.getInstance();
        final DeliveryState remoteState = new Declared();

        doAnswer(byRunningRunnable()).when(reactorDispatcher).invoke(any(Runnable.class));
        when(delivery.getRemoteState()).thenReturn(remoteState);
        when(delivery.remotelySettled()).thenReturn(true);

        try (ReceiverUnsettledDeliveries deliveries = createUnsettledDeliveries()) {
            deliveries.onDelivery(deliveryTag, delivery);
            final Mono<Void> dispositionMono = deliveries.sendDisposition(deliveryTag.toString(), desiredState);
            StepVerifier.create(dispositionMono)
                .then(() -> deliveries.onDispositionAck(deliveryTag, delivery))
                .verifyErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(AmqpException.class, error);
                    Assertions.assertEquals(remoteState.toString(), error.getMessage());
                });
            verify(delivery).disposition(desiredState);
            Assertions.assertFalse(deliveries.containsDelivery(deliveryTag));
        }
    }

    @Test
    public void sendDispositionRetriesOnRejectedOutcome() throws IOException {
        final UUID deliveryTag = UUID.randomUUID();
        final DeliveryState desiredState = Accepted.getInstance();
        final Rejected remoteState = new Rejected();
        final ErrorCondition remoteError = new ErrorCondition(AmqpErrorCode.SERVER_BUSY_ERROR, null);
        remoteState.setError(remoteError);
        final int[] dispositionCallCount = new int[1];

        doAnswer(byRunningRunnable()).when(reactorDispatcher).invoke(any(Runnable.class));
        when(delivery.getRemoteState()).thenReturn(remoteState);
        when(delivery.remotelySettled()).thenReturn(true);

        try (ReceiverUnsettledDeliveries deliveries = createUnsettledDeliveries()) {
            doAnswer(__ -> {
                final boolean firstDispositionCall = dispositionCallCount[0] == 0;
                if (!firstDispositionCall) {
                    // See note below on why this doAnswer skips onDispositionAck for the first disposition call.
                    deliveries.onDispositionAck(deliveryTag, delivery);
                }
                dispositionCallCount[0]++;
                return null;
            }).when(delivery).disposition(any());

            deliveries.onDelivery(deliveryTag, delivery);
            final Mono<Void> dispositionMono = deliveries.sendDisposition(deliveryTag.toString(), desiredState);
            StepVerifier.create(dispositionMono)
                // Inside sendDisposition(,) implementation, it calls delivery.disposition(state), but before the mock
                // (above doAnswer) responds with onDispositionAck, it is required that sendDisposition(,) insert
                // the deliveryTag into ReceiverUnsettledDeliveries::pendingDispositions Map.
                // The ReceiverUnsettledDeliveries::onDispositionAck will look up the Map for the deliveryTag. So, if
                // mock responds to disposition call with onDispositionAck before the Map update, the test won’t be able
                // to validate the scenario. This is why the mock is not calling onDispositionAck when it is invoked for
                // first disposition(state) call, instead the test let the sendDisposition to complete it execution then
                // invoke
                // onDispositionAck(,) below to ack the first disposition(state) call.
                .then(() -> deliveries.onDispositionAck(deliveryTag, delivery))
                .verifyErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(AmqpException.class, error);
                    final AmqpException amqpError = (AmqpException) error;
                    Assertions.assertEquals(AmqpErrorCondition.SERVER_BUSY_ERROR, amqpError.getErrorCondition());
                });
            // Asserts that the retry exhausted.
            Assertions.assertEquals(RETRY_OPTIONS.getMaxRetries() + 1, dispositionCallCount[0]);
        }
    }

    @Test
    public void sendDispositionMonoCacheCompletion() throws IOException {
        final UUID deliveryTag = UUID.randomUUID();
        final DeliveryState desiredState = Accepted.getInstance();
        final DeliveryState remoteState = desiredState;
        final int[] dispositionCallCount = new int[1];

        doAnswer(byRunningRunnable()).when(reactorDispatcher).invoke(any(Runnable.class));
        when(delivery.getRemoteState()).thenReturn(remoteState);
        when(delivery.remotelySettled()).thenReturn(true);
        doAnswer(invocation -> {
            dispositionCallCount[0]++;
            return null;
        }).when(delivery).disposition(any());

        try (ReceiverUnsettledDeliveries deliveries = createUnsettledDeliveries()) {
            deliveries.onDelivery(deliveryTag, delivery);
            final Mono<Void> dispositionMono = deliveries.sendDisposition(deliveryTag.toString(), desiredState);
            StepVerifier.create(dispositionMono)
                .then(() -> deliveries.onDispositionAck(deliveryTag, delivery))
                .verifyComplete();
            for (int i = 0; i < 3; i++) {
                StepVerifier.create(dispositionMono).verifyComplete();
            }
            Assertions.assertEquals(1, dispositionCallCount[0]);
        }
    }

    @Test
    public void sendDispositionMonoCacheError() throws IOException {
        final UUID deliveryTag = UUID.randomUUID();
        final DeliveryState desiredState = Accepted.getInstance();
        final DeliveryState remoteState = new Declared();
        final int[] dispositionCallCount = new int[1];

        doAnswer(byRunningRunnable()).when(reactorDispatcher).invoke(any(Runnable.class));
        when(delivery.getRemoteState()).thenReturn(remoteState);
        when(delivery.remotelySettled()).thenReturn(true);
        doAnswer(invocation -> {
            dispositionCallCount[0]++;
            return null;
        }).when(delivery).disposition(any());

        try (ReceiverUnsettledDeliveries deliveries = createUnsettledDeliveries()) {
            deliveries.onDelivery(deliveryTag, delivery);
            final Mono<Void> dispositionMono = deliveries.sendDisposition(deliveryTag.toString(), desiredState);
            final Throwable[] lastError = new Throwable[1];
            StepVerifier.create(dispositionMono)
                .then(() -> deliveries.onDispositionAck(deliveryTag, delivery))
                .verifyErrorSatisfies(error -> lastError[0] = error);
            for (int i = 0; i < 3; i++) {
                StepVerifier.create(dispositionMono).verifyErrorSatisfies(error -> {
                    Assertions.assertEquals(lastError[0], error,
                        "Expected replay of the last error object, but received a new error object.");
                });
            }
            Assertions.assertEquals(1, dispositionCallCount[0]);
        }
    }

    @Test
    public void pendingSendDispositionErrorsOnClose() throws IOException {
        final UUID deliveryTag = UUID.randomUUID();
        final DeliveryState desiredState = Accepted.getInstance();
        final DeliveryState remoteState = new Declared();

        doAnswer(byRunningRunnable()).when(reactorDispatcher).invoke(any(Runnable.class));
        when(delivery.getRemoteState()).thenReturn(remoteState);
        when(delivery.remotelySettled()).thenReturn(true);

        final ReceiverUnsettledDeliveries deliveries = createUnsettledDeliveries();
        try {
            deliveries.onDelivery(deliveryTag, delivery);
            final Mono<Void> dispositionMono = deliveries.sendDisposition(deliveryTag.toString(), desiredState);
            StepVerifier.create(dispositionMono).then(() -> deliveries.close()).verifyErrorSatisfies(error -> {
                Assertions.assertInstanceOf(AmqpException.class, error);
                Assertions.assertEquals(DISPOSITION_ERROR_ON_CLOSE, error.getMessage());
            });
        } finally {
            deliveries.close();
        }
    }

    @Test
    public void shouldTerminateAndAwaitForDispositionInProgressToComplete() throws IOException {
        final UUID deliveryTag = UUID.randomUUID();
        final DeliveryState desiredState = Accepted.getInstance();
        final DeliveryState remoteState = desiredState;

        doAnswer(byRunningRunnable()).when(reactorDispatcher).invoke(any(Runnable.class));
        when(delivery.getRemoteState()).thenReturn(remoteState);
        when(delivery.remotelySettled()).thenReturn(true);

        try (ReceiverUnsettledDeliveries deliveries = createUnsettledDeliveries()) {
            deliveries.onDelivery(deliveryTag, delivery);
            final Mono<Void> dispositionMono = deliveries.sendDisposition(deliveryTag.toString(), desiredState);
            dispositionMono.subscribe();

            StepVerifier.create(deliveries.terminateAndAwaitForDispositionsInProgressToComplete())
                .then(() -> deliveries.onDispositionAck(deliveryTag, delivery))
                .verifyComplete();

            StepVerifier.create(dispositionMono).verifyComplete();
        }
    }

    @Test
    public void closeDoNotWaitForSendDispositionCompletion() throws IOException {
        final UUID deliveryTag = UUID.randomUUID();
        final DeliveryState desiredState = Accepted.getInstance();
        final DeliveryState remoteState = desiredState;

        doAnswer(byRunningRunnable()).when(reactorDispatcher).invoke(any(Runnable.class));
        when(delivery.getRemoteState()).thenReturn(remoteState);
        when(delivery.remotelySettled()).thenReturn(true);

        final ReceiverUnsettledDeliveries deliveries = createUnsettledDeliveries();
        try {
            deliveries.onDelivery(deliveryTag, delivery);
            final Mono<Void> dispositionMono = deliveries.sendDisposition(deliveryTag.toString(), desiredState);
            dispositionMono.subscribe();

            StepVerifier.create(Mono.<Void>fromRunnable(() -> deliveries.close()))
                .then(() -> deliveries.onDispositionAck(deliveryTag, delivery))
                .verifyComplete();

            StepVerifier.create(dispositionMono).verifyErrorSatisfies(error -> {
                Assertions.assertInstanceOf(AmqpException.class, error);
                Assertions.assertEquals(DISPOSITION_ERROR_ON_CLOSE, error.getMessage());
            });
        } finally {
            deliveries.close();
        }
    }

    @Test
    public void nopOnDeliveryOnceClosed() {
        final UUID deliveryTag = UUID.randomUUID();

        final ReceiverUnsettledDeliveries deliveries = createUnsettledDeliveries();
        try {
            deliveries.close();
            Assertions.assertFalse(deliveries.onDelivery(deliveryTag, delivery));
            Assertions.assertFalse(deliveries.containsDelivery(deliveryTag));
        } finally {
            deliveries.close();
        }
    }

    @Test
    @Disabled("Enable in once disposition API exposed in ReceiveLinkHandler")
    public void settlesUnsettledDeliveriesOnClose() throws IOException {
        // See the notes in ReceiverUnsettledDeliveries.close()
        //
        doAnswer(byRunningRunnable()).when(reactorDispatcher).invoke(any(Runnable.class));

        final ReceiverUnsettledDeliveries deliveries = createUnsettledDeliveries();
        try {
            deliveries.onDelivery(UUID.randomUUID(), delivery);
            deliveries.close();
            verify(delivery).disposition(any());
            verify(delivery).settle();
        } finally {
            deliveries.close();
        }
    }

    private ReceiverUnsettledDeliveries createUnsettledDeliveries() {
        return new ReceiverUnsettledDeliveries(HOSTNAME, ENTITY_PATH, RECEIVER_LINK_NAME, reactorDispatcher,
            RETRY_OPTIONS, DELIVERY_EMPTY_TAG, LOGGER);
    }

    private static Answer<Void> byRunningRunnable() {
        return invocation -> {
            final Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        };
    }
}
