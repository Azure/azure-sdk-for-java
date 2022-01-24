// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import org.apache.qpid.proton.engine.Record;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.Selectable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.time.Duration;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReactorDispatcherTest {
    private static final Duration VERIFY_TIMEOUT = Duration.ofSeconds(5);

    @Mock
    private Reactor reactor;
    @Mock
    private Selectable selectable;
    @Mock
    private Record attachments;
    @Mock
    private Runnable work;
    @Mock
    private Pipe pipe;
    @Mock
    private Pipe.SinkChannel sinkChannel;
    @Mock
    private Pipe.SourceChannel sourceChannel;

    private final AtomicReference<Selectable.Callback> closeScheduler = new AtomicReference<>();
    private final AtomicReference<Selectable.Callback> workScheduler = new AtomicReference<>();
    private AutoCloseable mockCloseable;

    @BeforeEach
    public void beforeEach() throws IOException {
        mockCloseable = MockitoAnnotations.openMocks(this);

        doAnswer(invocation -> {
            final Selectable.Callback callback = invocation.getArgument(0);
            closeScheduler.set(callback);
            return null;
        }).when(selectable).onFree(any(Selectable.Callback.class));

        doAnswer(invocation -> {
            final Selectable.Callback callback = invocation.getArgument(0);
            workScheduler.set(callback);
            return null;
        }).when(selectable).onReadable(any(Selectable.Callback.class));

        when(reactor.selectable()).thenReturn(selectable);
        when(reactor.attachments()).thenReturn(attachments);

        when(pipe.sink()).thenReturn(sinkChannel);
        when(pipe.source()).thenReturn(sourceChannel);
    }

    @AfterEach
    public void afterEach() throws Exception {
        Mockito.reset(reactor, selectable, pipe, sinkChannel, sourceChannel);

        if (mockCloseable != null) {
            mockCloseable.close();
        }
    }

    @Test
    public void sendsShutdownSignal() throws IOException {
        // Arrange
        when(attachments.get(eq(RejectedExecutionException.class), eq(RejectedExecutionException.class)))
            .thenReturn(null);

        when(sourceChannel.isOpen()).thenReturn(true);

        when(sinkChannel.isOpen()).thenReturn(true);
        when(sinkChannel.write(any(ByteBuffer.class))).thenReturn(1);

        final ReactorDispatcher reactorDispatcher = new ReactorDispatcher("foo-bar", reactor, pipe);

        // Act & Assert
        StepVerifier.create(reactorDispatcher.getShutdownSignal())
            .then(() -> {
                try {
                    reactorDispatcher.invoke(work);
                } catch (IOException e) {
                    fail("Encountered exception. " + e);
                }

                final Selectable.Callback callback = closeScheduler.get();
                assertNotNull(callback);
                callback.run(selectable);
            })
            .assertNext(signal -> {
                assertFalse(signal.isInitiatedByClient());
                assertFalse(signal.isTransient());
            })
            .expectComplete()
            .verify(VERIFY_TIMEOUT);

        verify(sourceChannel).close();
        verify(sinkChannel).close();
    }

    @Test
    public void sendsErrorSignal() throws IOException {
        // Arrange
        final IOException testException = new IOException("Test-error");
        when(attachments.get(eq(RejectedExecutionException.class), eq(RejectedExecutionException.class)))
            .thenReturn(null);

        when(sourceChannel.isOpen()).thenReturn(true);
        when(sourceChannel.read(any(ByteBuffer.class)))
            .thenReturn(10)
            .thenThrow(testException);

        when(sinkChannel.isOpen()).thenReturn(true);
        when(sinkChannel.write(any(ByteBuffer.class))).thenReturn(1);

        final ReactorDispatcher reactorDispatcher = new ReactorDispatcher("foo-bar", reactor, pipe);

        // Act & Assert
        StepVerifier.create(reactorDispatcher.getShutdownSignal())
            .then(() -> {
                final Selectable.Callback callback = workScheduler.get();
                assertNotNull(callback);
                callback.run(selectable);
            })
            .expectErrorMatches(error -> error instanceof RuntimeException
                && error.getCause().equals(testException))
            .verify(VERIFY_TIMEOUT);
    }
}
