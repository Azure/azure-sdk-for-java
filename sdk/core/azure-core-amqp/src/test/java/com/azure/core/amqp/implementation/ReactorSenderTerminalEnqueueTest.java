// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.handler.SendLinkHandler;
import com.azure.core.test.utils.metrics.TestMeter;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnsignedLong;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.codec.CompositeReadableBuffer;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Tests terminal sender races and cancellation of queued encoded sends. */
@Execution(ExecutionMode.SAME_THREAD)
public class ReactorSenderTerminalEnqueueTest {
    private static final Duration VERIFY_TIMEOUT = Duration.ofSeconds(2);
    private static final String ENTITY_PATH = "entity-path";
    private static final String HOSTNAME = "hostname";

    private Sender sender;
    private SendLinkHandler handler;
    private ReactorSender reactorSender;
    private TestFixture fixture;

    @BeforeEach
    void beforeEach() throws Exception {
        fixture = new TestFixture();
        sender = fixture.sender;
        handler = fixture.handler;
        reactorSender = fixture.reactorSender;
    }

    @AfterEach
    void afterEach() {
        if (handler != null) {
            handler.close();
        }
    }

    static Stream<Arguments> terminalAndEncoding() {
        return Stream.of(Arguments.of(false, false), Arguments.of(false, true), Arguments.of(true, false),
            Arguments.of(true, true));
    }

    @ParameterizedTest(name = "handlerError={0}, batch={1}")
    @MethodSource("terminalAndEncoding")
    void terminalDuringEncodingFailsTransientAndClearsPending(boolean handlerError, boolean batch) throws Exception {
        AtomicBoolean closedDuringEncoding = new AtomicBoolean();
        Message message = mock(Message.class);
        when(fixture.serializer.getSize(message)).thenReturn(10);
        when(message.encode(any(byte[].class), anyInt(), anyInt())).thenAnswer(invocation -> {
            if (closedDuringEncoding.compareAndSet(false, true)) {
                closeHandler(handlerError);
                assertTrue(reactorSender.isDisposed());
            }
            return 1;
        });
        Mono<Void> send = batch ? reactorSender.send(Arrays.asList(message, message)) : reactorSender.send(message);
        StepVerifier.create(send).expectErrorSatisfies(error -> {
            assertInstanceOf(AmqpException.class, error);
            assertTrue(((AmqpException) error).isTransient());
        }).verify(VERIFY_TIMEOUT);
        assertTrue(closedDuringEncoding.get());
        assertEquals(0, pendingMapSize());
        assertEquals(0, pendingQueueSize());
        Mockito.verify(sender, Mockito.never()).delivery(any());
        assertTrue(
            fixture.meter.getHistograms().get("messaging.az.amqp.producer.send.duration").getMeasurements().isEmpty());
    }

    @ParameterizedTest(name = "readableBuffer={0}")
    @MethodSource("encodedForms")
    void queuedSendTimeoutCancellationRemovesWorkForByteArrayAndReadableBuffer(boolean readableBuffer) {
        Mono<?> send = readableBuffer
            ? reactorSender.send(new CompositeReadableBuffer().append(new byte[] { 1, 2 }), 0, null)
            : reactorSender.send(new byte[] { 1, 2 }, 0, 0, null);
        VirtualTimeScheduler timeoutScheduler = VirtualTimeScheduler.create();

        try {
            StepVerifier.create(send.timeout(Duration.ofSeconds(1), timeoutScheduler)).then(() -> {
                assertEquals(1, pendingMapSize());
                assertEquals(1, pendingQueueSize());
            })
                .then(() -> timeoutScheduler.advanceTimeBy(Duration.ofSeconds(1)))
                .expectError(java.util.concurrent.TimeoutException.class)
                .verify(VERIFY_TIMEOUT);
        } finally {
            timeoutScheduler.dispose();
        }

        assertEquals(0, pendingMapSize());
        assertEquals(0, pendingQueueSize());
    }

    @ParameterizedTest(name = "readableBuffer={0}")
    @MethodSource("encodedForms")
    void closureAfterEnqueueErrorsAndClearsWorkForByteArrayAndReadableBuffer(boolean readableBuffer) throws Exception {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch complete = new CountDownLatch(1);
        Mono<?> send = readableBuffer
            ? reactorSender.send(new CompositeReadableBuffer().append(new byte[] { 1, 2 }), 0, null)
            : reactorSender.send(new byte[] { 1, 2 }, 0, 0, null);
        send.subscribe(ignored -> {
        }, failure -> {
            error.set(failure);
            complete.countDown();
        }, complete::countDown);

        assertEquals(1, pendingMapSize());
        assertEquals(1, pendingQueueSize());
        closeHandler(false);

        assertTrue(complete.await(VERIFY_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS));
        assertInstanceOf(AmqpException.class, error.get());
        assertTrue(((AmqpException) error.get()).isTransient());
        assertEquals(0, pendingMapSize());
        assertEquals(0, pendingQueueSize());
        assertTrue(
            fixture.meter.getHistograms().get("messaging.az.amqp.producer.send.duration").getMeasurements().isEmpty());
    }

    @Test
    void sendStartedAfterClosureFailsWithoutQueueingForSingleAndBatch() {
        closeHandler(false);

        StepVerifier.create(reactorSender.send(mock(Message.class)))
            .expectError(IllegalStateException.class)
            .verify(VERIFY_TIMEOUT);
        StepVerifier.create(reactorSender.send(Arrays.asList(mock(Message.class), mock(Message.class))))
            .expectError(IllegalStateException.class)
            .verify(VERIFY_TIMEOUT);

        assertEquals(0, pendingMapSize());
        assertEquals(0, pendingQueueSize());
    }

    @Test
    void cancellationDuringDelayedRetryDoesNotRequeueAndDoesNotStallLaterSend() throws Exception {
        AtomicReference<Delivery> firstDelivery = new AtomicReference<>();
        when(sender.delivery(any())).thenAnswer(invocation -> {
            Delivery delivery = mock(Delivery.class);
            when(delivery.getTag()).thenReturn(invocation.getArgument(0));
            when(delivery.getLink()).thenReturn(sender);
            when(delivery.getRemoteState())
                .thenReturn(fixture.deliveries.isEmpty() ? rejectedState() : Accepted.getInstance());
            fixture.deliveries.add(delivery);
            firstDelivery.compareAndSet(null, delivery);
            return delivery;
        });
        when(sender.advance()).thenReturn(true);

        Disposable subscription = reactorSender.send(new byte[] { 1 }, 0, 0, null).subscribe();
        fixture.runNextDispatcherWork();
        assertEquals(1, fixture.deliveries.size());

        Event deliveryEvent = mock(Event.class);
        when(deliveryEvent.getDelivery()).thenReturn(firstDelivery.get());
        handler.onDelivery(deliveryEvent);
        assertEquals(1, fixture.delayedWork.size());

        subscription.dispose();
        fixture.delayedWork.get(0).run();

        assertEquals(0, pendingMapSize());
        assertEquals(0, pendingQueueSize());

        AtomicReference<Throwable> laterError = new AtomicReference<>();
        CountDownLatch laterComplete = new CountDownLatch(1);
        Disposable laterSend = reactorSender.send(new byte[] { 2 }, 0, 0, null).subscribe(ignored -> {
        }, failure -> {
            laterError.set(failure);
            laterComplete.countDown();
        }, laterComplete::countDown);
        assertEquals(1, pendingMapSize());
        fixture.runNextDispatcherWork();
        Event acceptedEvent = mock(Event.class);
        when(acceptedEvent.getDelivery()).thenReturn(fixture.deliveries.get(1));
        handler.onDelivery(acceptedEvent);
        assertTrue(laterComplete.await(VERIFY_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS));
        Assertions.assertNull(laterError.get());
        assertEquals(1,
            fixture.meter.getHistograms().get("messaging.az.amqp.producer.send.duration").getMeasurements().size());
        laterSend.dispose();
    }

    private void closeHandler(boolean error) {
        when(sender.getLocalState()).thenReturn(EndpointState.CLOSED);
        when(sender.getRemoteState()).thenReturn(EndpointState.CLOSED);
        when(sender.getRemoteCondition()).thenReturn(error
            ? new ErrorCondition(Symbol.getSymbol(AmqpErrorCondition.SERVER_BUSY_ERROR.getErrorCondition()),
                "server busy")
            : null);
        Event closeEvent = mock(Event.class);
        when(closeEvent.getLink()).thenReturn(sender);
        handler.onLinkRemoteClose(closeEvent);
    }

    private Rejected rejectedState() {
        Rejected rejected = new Rejected();
        rejected.setError(new ErrorCondition(Symbol.getSymbol(AmqpErrorCondition.SERVER_BUSY_ERROR.getErrorCondition()),
            "server busy"));
        return rejected;
    }

    private int pendingMapSize() {
        return fixture.pendingMapSize();
    }

    private int pendingQueueSize() {
        return fixture.pendingQueueSize();
    }

    static Stream<Boolean> encodedForms() {
        return Stream.of(false, true);
    }

    private static final class TestFixture {
        private final AmqpConnection connection = mock(AmqpConnection.class);
        private final Sender sender = mock(Sender.class);
        private final ReactorProvider provider = mock(ReactorProvider.class);
        private final ReactorDispatcher dispatcher = mock(ReactorDispatcher.class);
        private final MessageSerializer serializer = mock(MessageSerializer.class);
        private final Scheduler scheduler = mock(Scheduler.class);
        private final TestMeter meter = new TestMeter();
        private final AmqpMetricsProvider metricsProvider = new AmqpMetricsProvider(meter, HOSTNAME, ENTITY_PATH);
        private final SendLinkHandler handler
            = new SendLinkHandler("connection-id", HOSTNAME, "link-name", ENTITY_PATH, metricsProvider);
        private final List<Runnable> dispatcherWork = new CopyOnWriteArrayList<>();
        private final List<Runnable> delayedWork = new CopyOnWriteArrayList<>();
        private final List<Delivery> deliveries = new CopyOnWriteArrayList<>();
        private final ReactorSender reactorSender;

        private TestFixture() throws Exception {
            when(connection.getShutdownSignals()).thenReturn(Flux.never());
            when(provider.getReactorDispatcher()).thenReturn(dispatcher);
            when(sender.getName()).thenReturn("link-name");
            when(sender.getRemoteMaxMessageSize()).thenReturn(UnsignedLong.valueOf(1024));
            when(sender.getRemoteTarget()).thenReturn(mock(Target.class));
            when(sender.getCredit()).thenReturn(100);
            when(sender.getRemoteProperties()).thenReturn(null);
            doAnswer(invocation -> {
                dispatcherWork.add(invocation.getArgument(0));
                return null;
            }).when(dispatcher).invoke(any(Runnable.class));
            doAnswer(invocation -> {
                delayedWork.add(invocation.getArgument(0));
                return null;
            }).when(dispatcher).invoke(any(Runnable.class), any(Duration.class));

            AmqpRetryOptions retryOptions = new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(10))
                .setMode(AmqpRetryMode.FIXED)
                .setDelay(Duration.ofMillis(10));
            reactorSender = new ReactorSender(connection, ENTITY_PATH, sender, handler, provider, null, serializer,
                retryOptions, scheduler, metricsProvider);

            Event openEvent = mock(Event.class);
            when(openEvent.getLink()).thenReturn(sender);
            handler.onLinkRemoteOpen(openEvent);
        }

        private void runNextDispatcherWork() {
            Assertions.assertFalse(dispatcherWork.isEmpty());
            dispatcherWork.remove(0).run();
        }

        @SuppressWarnings("unchecked")
        private int pendingMapSize() {
            try {
                java.lang.reflect.Field field = ReactorSender.class.getDeclaredField("pendingSendsMap");
                field.setAccessible(true);
                return ((java.util.Map<String, ?>) field.get(reactorSender)).size();
            } catch (ReflectiveOperationException exception) {
                throw new AssertionError(exception);
            }
        }

        private int pendingQueueSize() {
            try {
                java.lang.reflect.Field field = ReactorSender.class.getDeclaredField("pendingSendsQueue");
                field.setAccessible(true);
                return ((java.util.Queue<?>) field.get(reactorSender)).size();
            } catch (ReflectiveOperationException exception) {
                throw new AssertionError(exception);
            }
        }
    }
}
