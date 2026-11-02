// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.implementation.ServiceBusProcessorClientOptions;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.FutureTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Isolated("Replaces the shared Reactor scheduler factory")
public class ServiceBusProcessorAsyncClientSingleWorkerTest {
    private Schedulers.Snapshot originalSchedulers;
    private Scheduler singleWorker;
    private ServiceBusProcessorAsyncClient processor;
    private ServiceBusReceiverAsyncClient receiver;
    private ServiceBusSessionReceiverAsyncClient sessions;

    @BeforeEach
    public void setup() {
        originalSchedulers = Schedulers.setFactoryWithSnapshot(new Schedulers.Factory() {
            @Override
            public Scheduler newBoundedElastic(int threadCap, int queuedTaskCap, ThreadFactory threadFactory,
                int ttlSeconds) {
                return Schedulers.Factory.super.newBoundedElastic(1, queuedTaskCap, threadFactory, ttlSeconds);
            }
        });
        singleWorker = Schedulers.boundedElastic();
    }

    @AfterEach
    public void cleanup() {
        Schedulers.resetFrom(originalSchedulers);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void closeOnSingleWorkerFinishesSessionCleanup(boolean closeFails) {
        createProcessor();
        final IllegalStateException failure = new IllegalStateException("session close failed");
        if (closeFails) {
            doThrow(failure).when(receiver).close();
        }
        processor.start().block(Duration.ofSeconds(5));
        final Mono<Void> close = Mono.<Void>fromRunnable(processor::close).subscribeOn(singleWorker);
        if (closeFails) {
            assertSame(failure, assertThrows(IllegalStateException.class, () -> close.block(Duration.ofSeconds(5))));
        } else {
            close.block(Duration.ofSeconds(5));
        }
        verify(receiver).close();
        verify(sessions).close();
        assertFalse(processor.isRunning());
    }

    @Test
    public void monitorOnSingleWorkerFinishesSessionCleanup() {
        final ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder builder = createProcessor();
        when(sessions.isConnectionClosed()).thenReturn(true);
        processor.start().block(Duration.ofSeconds(5));
        verify(builder, timeout(15000).times(2)).buildAsyncClientForAsyncProcessor();
        verify(receiver).close();
        verify(sessions).close();
        processor.close();
    }

    @Test
    public void closeWaitsForConcurrentIdleCleanupWithoutClosingTwice() throws Exception {
        createProcessor();
        when(sessions.getSessionIdleTimeout()).thenReturn(Duration.ofMillis(20));
        final CountDownLatch closing = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final AtomicBoolean finished = new AtomicBoolean();
        doAnswer(invocation -> {
            assertFalse(Schedulers.isInNonBlockingThread());
            closing.countDown();
            assertTrue(release.await(5, TimeUnit.SECONDS));
            finished.set(true);
            return null;
        }).when(receiver).close();
        doAnswer(invocation -> {
            assertTrue(finished.get(), "The parent must outlive session receiver cleanup");
            return null;
        }).when(sessions).close();
        processor.start().block(Duration.ofSeconds(5));
        assertTrue(closing.await(5, TimeUnit.SECONDS));
        final FutureTask<Void> closed = new FutureTask<>(processor::close, null);
        new Thread(closed, "processor-close").start();
        try {
            assertThrows(TimeoutException.class, () -> closed.get(100, TimeUnit.MILLISECONDS));
            verify(sessions, never()).close();
        } finally {
            release.countDown();
            closed.get(5, TimeUnit.SECONDS);
        }
        verify(receiver).close();
        verify(sessions).close();
    }

    private ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder createProcessor() {
        final ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder builder
            = mock(ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder.class);
        receiver = mock(ServiceBusReceiverAsyncClient.class);
        sessions = mock(ServiceBusSessionReceiverAsyncClient.class);
        when(builder.buildAsyncClientForAsyncProcessor()).thenReturn(sessions);
        when(sessions.getFullyQualifiedNamespace()).thenReturn("namespace");
        when(sessions.getEntityPath()).thenReturn("entity");
        when(sessions.getSessionIdleTimeout()).thenReturn(Duration.ofMinutes(1));
        when(sessions.getReceiverOptions()).thenReturn(ReceiverOptions.createUnnamedSessionOptions(
            ServiceBusReceiveMode.PEEK_LOCK, 0, Duration.ZERO, false, 1, Duration.ofMinutes(1)));
        when(sessions.acceptNextSession()).thenReturn(Mono.just(receiver)).thenReturn(Mono.never());
        when(receiver.receiveMessagesWithContextForAsyncProcessor()).thenReturn(Flux.never());
        processor = new ServiceBusProcessorAsyncClient(builder, "entity", null, null, context -> Mono.empty(),
            error -> Mono.empty(),
            new ServiceBusProcessorClientOptions().setMaxConcurrentCalls(1).setDrainTimeout(Duration.ofMillis(50)));
        return builder;
    }
}
