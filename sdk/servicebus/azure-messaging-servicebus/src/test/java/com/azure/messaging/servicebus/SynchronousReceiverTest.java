// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.instrumentation.ReceiverKind;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusReceiverInstrumentation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.azure.messaging.servicebus.models.ServiceBusReceiveMode.PEEK_LOCK;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class SynchronousReceiverTest {
    private static final String NAMESPACE = "namespace";
    private static final String ENTITY_PATH = "entity-path";
    private static final ClientLogger LOGGER = new ClientLogger(SynchronousReceiverTest.class);
    private static final ServiceBusReceiverInstrumentation NO_INSTRUMENTATION
        = new ServiceBusReceiverInstrumentation(null, null, NAMESPACE, ENTITY_PATH, null, ReceiverKind.SYNC_RECEIVER);
    private AutoCloseable mocksCloseable;

    @BeforeEach
    void setup() {
        mocksCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void teardown() throws Exception {
        Mockito.framework().clearInlineMock(this);

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @Test
    public void shouldErrorIterableStreamIfDisposed() {
        final int maxMessages = 1;
        final Duration maxWaitTime = Duration.ofMillis(500);
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);
        final ReceiverOptions receiverOptions
            = ReceiverOptions.createNonSessionOptions(PEEK_LOCK, 0, Duration.ZERO, false);

        when((asyncClient.getReceiverOptions())).thenReturn(receiverOptions);
        when(asyncClient.isV2()).thenReturn(true);
        when(asyncClient.getInstrumentation()).thenReturn(NO_INSTRUMENTATION);

        final SynchronousReceiver receiver = new SynchronousReceiver(LOGGER, asyncClient);
        receiver.dispose();

        final RuntimeException e = Assertions.assertThrows(RuntimeException.class, () -> {
            receiver.receive(maxMessages, maxWaitTime).stream().collect(Collectors.toList());
        });
        Assertions.assertNotNull(e.getCause());
        Assertions.assertEquals("Disposed.", e.getCause().getMessage());
    }

    @Test
    public void shouldSubscribeToUpstreamOnlyOnce() {
        final int maxMessages = 1;
        final Duration maxWaitTime = Duration.ofMillis(250);
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);
        final ReceiverOptions receiverOptions
            = ReceiverOptions.createNonSessionOptions(PEEK_LOCK, 0, Duration.ZERO, false);
        final TestPublisher<ServiceBusReceivedMessage> upstream = TestPublisher.create();

        when((asyncClient.getReceiverOptions())).thenReturn(receiverOptions);
        when(asyncClient.isV2()).thenReturn(true);
        when(asyncClient.getInstrumentation()).thenReturn(NO_INSTRUMENTATION);
        when(asyncClient.nonSessionSyncReceiveV2()).thenReturn(upstream.flux());

        final SynchronousReceiver receiver = new SynchronousReceiver(LOGGER, asyncClient);

        final IterableStream<ServiceBusReceivedMessage> iterable0 = receiver.receive(maxMessages, maxWaitTime);
        final IterableStream<ServiceBusReceivedMessage> iterable1 = receiver.receive(maxMessages, maxWaitTime);
        iterable0.stream().collect(Collectors.toList());
        iterable1.stream().collect(Collectors.toList());
        upstream.assertSubscribers(1);
    }

    @Test
    public void shouldInvokeReleaserWhenPrefetchDisabled() {
        final int prefetch = 0; // prefetch is disabled
        final int maxMessages = 5;
        final Duration maxWaitTime = Duration.ofMillis(250);
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);
        final ServiceBusReceivedMessage message0 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message1 = mock(ServiceBusReceivedMessage.class);
        final ArgumentCaptor<ServiceBusReceivedMessage> messageCaptor
            = ArgumentCaptor.forClass(ServiceBusReceivedMessage.class);
        final ReceiverOptions receiverOptions
            = ReceiverOptions.createNonSessionOptions(PEEK_LOCK, prefetch, Duration.ZERO, false);
        final Sinks.Many<ServiceBusReceivedMessage> upstream = Sinks.many().multicast().onBackpressureBuffer();

        when((asyncClient.getReceiverOptions())).thenReturn(receiverOptions);
        when(asyncClient.isV2()).thenReturn(true);
        when(asyncClient.getInstrumentation()).thenReturn(NO_INSTRUMENTATION);
        when(asyncClient.nonSessionSyncReceiveV2()).thenReturn(upstream.asFlux());
        when(asyncClient.release(any())).thenReturn(Mono.empty());

        final SynchronousReceiver receiver = new SynchronousReceiver(LOGGER, asyncClient);

        upstream.emitNext(message0, Sinks.EmitFailureHandler.FAIL_FAST);
        final IterableStream<ServiceBusReceivedMessage> iterable = receiver.receive(maxMessages, maxWaitTime);
        final List<ServiceBusReceivedMessage> list = iterable.stream().collect(Collectors.toList());
        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals(message0, list.get(0));

        final Sinks.EmitResult emitResult = upstream.tryEmitNext(message1);
        Assertions.assertEquals(Sinks.EmitResult.OK, emitResult);
        try {
            // The earlier receive() call has a timer-thread to complete the receiving when 'maxWaitTime' (250ms) expires.
            // It is possible that when test-thread signals 'message1' to drain-loop, the timer-thread is still in the
            // drain-loop, resulting the test-thread to continue the test run concurrently with timer-thread.
            // In such a setup, the "verify(asyncClient).release(messageCaptor.capture())" by test-thread will fail if
            // the timer-thread is yet to call 'release'. So, the test-thread sleeps here giving some time for timer-thread
            // to be done.
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Assertions.fail(e);
        }
        verify(asyncClient).release(messageCaptor.capture());
        verify(asyncClient, times(1)).release(any());
        final ServiceBusReceivedMessage releasedMessage = messageCaptor.getValue();
        Assertions.assertEquals(message1, releasedMessage);
    }

    @Test
    public void shouldNotInvokeReleaserWhenPrefetchEnabled() {
        final int prefetch = 1; // prefetch is enabled
        final int maxMessages = 5;
        final Duration maxWaitTime = Duration.ofMillis(250);
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);
        final ServiceBusReceivedMessage message0 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message1 = mock(ServiceBusReceivedMessage.class);
        final ReceiverOptions receiverOptions
            = ReceiverOptions.createNonSessionOptions(PEEK_LOCK, prefetch, Duration.ZERO, false);
        final Sinks.Many<ServiceBusReceivedMessage> upstream = Sinks.many().multicast().onBackpressureBuffer();

        when((asyncClient.getReceiverOptions())).thenReturn(receiverOptions);
        when(asyncClient.isV2()).thenReturn(true);
        when(asyncClient.getInstrumentation()).thenReturn(NO_INSTRUMENTATION);
        when(asyncClient.nonSessionSyncReceiveV2()).thenReturn(upstream.asFlux());
        when(asyncClient.release(any())).thenReturn(Mono.empty());

        final SynchronousReceiver receiver = new SynchronousReceiver(LOGGER, asyncClient);

        upstream.emitNext(message0, Sinks.EmitFailureHandler.FAIL_FAST);
        final IterableStream<ServiceBusReceivedMessage> iterable = receiver.receive(maxMessages, maxWaitTime);
        final List<ServiceBusReceivedMessage> list = iterable.stream().collect(Collectors.toList());
        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals(message0, list.get(0));

        final Sinks.EmitResult emitResult = upstream.tryEmitNext(message1);
        Assertions.assertEquals(Sinks.EmitResult.OK, emitResult);
        try {
            // The earlier receive() call has a timer-thread to complete the receiving when 'maxWaitTime' (250ms) expires.
            // It is possible that when test-thread signals 'message1' to drain-loop, the timer-thread is still in the
            // drain-loop, resulting the test-thread to continue the test run concurrently with timer-thread.
            // If the timer-thread calls 'release' (which it should not since prefetch is enabled) after the
            // "verify(asyncClient, times(0)).release(any())" check by test-thread, then, the test won't catch this
            // unexpected 'release' call. So, the test-thread sleeps here giving some time for timer-thread to be done.
            Thread.sleep(250);
        } catch (InterruptedException e) {
            Assertions.fail(e);
        }
        verify(asyncClient, times(0)).release(any());
    }

    @Test
    public void shouldCancelUpstreamOnDispose() {
        final int prefetch = 0;
        final int maxMessages = 2;
        final Duration maxWaitTime = Duration.ofMillis(250);
        final ServiceBusReceiverAsyncClient asyncClient = mock(ServiceBusReceiverAsyncClient.class);
        final ServiceBusReceivedMessage message0 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message1 = mock(ServiceBusReceivedMessage.class);
        final ReceiverOptions receiverOptions
            = ReceiverOptions.createNonSessionOptions(PEEK_LOCK, prefetch, Duration.ZERO, false);
        final Sinks.Many<ServiceBusReceivedMessage> upstream = Sinks.many().multicast().onBackpressureBuffer();
        final AtomicBoolean upstreamCanceled = new AtomicBoolean(false);

        when((asyncClient.getReceiverOptions())).thenReturn(receiverOptions);
        when(asyncClient.isV2()).thenReturn(true);
        when(asyncClient.getInstrumentation()).thenReturn(NO_INSTRUMENTATION);
        when(asyncClient.nonSessionSyncReceiveV2())
            .thenReturn(upstream.asFlux().doOnCancel(() -> upstreamCanceled.set(true)));

        final SynchronousReceiver receiver = new SynchronousReceiver(LOGGER, asyncClient);

        upstream.emitNext(message0, Sinks.EmitFailureHandler.FAIL_FAST);
        upstream.emitNext(message1, Sinks.EmitFailureHandler.FAIL_FAST);
        final IterableStream<ServiceBusReceivedMessage> iterable = receiver.receive(maxMessages, maxWaitTime);
        final List<ServiceBusReceivedMessage> list = iterable.stream().collect(Collectors.toList());
        Assertions.assertEquals(2, list.size());
        Assertions.assertEquals(Arrays.asList(message0, message1), list);
        Assertions.assertFalse(upstreamCanceled.get());

        receiver.dispose();
        Assertions.assertTrue(upstreamCanceled.get());
    }
}
