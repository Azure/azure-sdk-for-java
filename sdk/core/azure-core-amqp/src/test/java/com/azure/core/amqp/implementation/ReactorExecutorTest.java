// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.exception.AmqpException;
import org.apache.qpid.proton.engine.Handler;
import org.apache.qpid.proton.engine.HandlerException;
import org.apache.qpid.proton.engine.Record;
import org.apache.qpid.proton.reactor.Reactor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.scheduler.Scheduler;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ReactorExecutor}.
 */
public class ReactorExecutorTest {
    private static final String HOSTNAME = "test-hostname";
    private static final String CONNECTION_ID = "connection-id";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    @Mock
    private Reactor reactor;
    @Mock
    private Scheduler scheduler;
    @Mock
    private AmqpExceptionHandler exceptionHandler;

    private AutoCloseable mocksCloseable;

    @BeforeEach
    public void beforeEach() {
        mocksCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void afterEach() throws Exception {
        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    /**
     * It only starts the reactor once.
     */
    @Test
    public void startsOnce() {
        // Arrange
        final ReactorExecutor executor = new ReactorExecutor(reactor, scheduler, CONNECTION_ID, exceptionHandler,
            TIMEOUT, HOSTNAME);

        doAnswer(invocation -> {
            return null;
        }).when(scheduler).schedule(any(Runnable.class));

        // Act
        executor.start();

        verify(reactor).start();
        verify(scheduler).schedule(any(Runnable.class));

        // The reactor is only started once even though invoked twice.
        executor.start();

        verify(reactor).start();
        verify(scheduler).schedule(any(Runnable.class));
    }

    /**
     * The closeAsync completes when the reactor has not been started.
     */
    @Test
    public void closesWhenNotRun() {
        // Arrange
        // Scheduling pending work adds the timeout. So we'll give at most timeout *2.
        final Duration timeout = TIMEOUT.plus(TIMEOUT);

        final ReactorExecutor executor = new ReactorExecutor(reactor, scheduler, CONNECTION_ID, exceptionHandler,
            TIMEOUT, HOSTNAME);

        doAnswer(invocation -> {
            return null;
        }).when(scheduler).schedule(any(Runnable.class));

        // Act & Verify
        StepVerifier.create(executor.closeAsync())
            .expectComplete()
            .verify(timeout);

        // Verify that it returns the same completed result.
        StepVerifier.create(executor.closeAsync())
            .expectComplete()
            .verify(timeout);

        verify(exceptionHandler).onConnectionShutdown(
            argThat(shutdown -> !shutdown.isTransient() && shutdown.isInitiatedByClient()));
    }

    /**
     * We cannot start running the reactor after it has been closed.
     */
    @Test
    public void cannotProcessAfterClosing() {
        // Arrange
        // Scheduling pending work adds the timeout. So we'll give at most timeout *2.
        final Duration timeout = TIMEOUT.plus(TIMEOUT);

        final ReactorExecutor executor = new ReactorExecutor(reactor, scheduler, CONNECTION_ID, exceptionHandler,
            TIMEOUT, HOSTNAME);
        final AtomicInteger timesInvoked = new AtomicInteger();

        doAnswer(invocation -> {
            if (timesInvoked.getAndIncrement() >= 3) {
                throw new IllegalStateException("Could not invoke runnable item. Test scheduler.");
            }

            final Runnable work = invocation.getArgument(0);
            work.run();
            return null;
        }).when(scheduler).schedule(any(Runnable.class));

        StepVerifier.create(executor.closeAsync())
            .expectComplete()
            .verify(timeout);

        // Act
        executor.start();

        // Assert
        // Verify that we only scheduled the initial run work, but it stopped there.
        verify(scheduler, never()).schedule(any(Runnable.class));
        verify(reactor, never()).start();
        verify(reactor, never()).process();

        verify(exceptionHandler).onConnectionShutdown(
            argThat(shutdown -> !shutdown.isTransient() && shutdown.isInitiatedByClient()));
    }

    /**
     * Reschedules the ReactorExecutor run method until not returned has occurred.
     */
    @Test
    public void reschedulesSuccessfullyThenStops() throws InterruptedException {
        // Arrange
        final ReactorExecutor executor = new ReactorExecutor(reactor, scheduler, CONNECTION_ID, exceptionHandler,
            TIMEOUT, HOSTNAME);
        final Semaphore semaphore = new Semaphore(1);
        semaphore.acquire();

        doAnswer(invocation -> {
            final Runnable work = invocation.getArgument(0);
            work.run();
            return null;
        }).when(scheduler).schedule(any(Runnable.class));

        // This is invoked when we schedule pending tasks before closing serializer.
        doAnswer(invocation -> {
            final Runnable work = invocation.getArgument(0);
            work.run();
            semaphore.release();
            return null;
        }).when(scheduler).schedule(any(Runnable.class), eq(TIMEOUT.toMillis()), eq(TimeUnit.MILLISECONDS));

        final AtomicInteger timesProcessed = new AtomicInteger();
        doAnswer(invocation -> timesProcessed.getAndIncrement() < 1)
            .when(reactor).process();

        // Act
        executor.start();

        final boolean acquired = semaphore.tryAcquire(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        assertTrue(acquired, "Unable to stop reactor when there are no more items to process.");

        // Assert
        verify(reactor).start();
        verify(reactor).stop();
        verify(reactor).free();

        // 1st time: success in run();
        // 2nd time: returns false in run()
        // 3rd time: schedulePendingTasks()
        verify(reactor, times(3)).process();
        verify(scheduler).dispose();

        verify(exceptionHandler).onConnectionShutdown(argThat(shutdown -> {
            return !shutdown.isTransient() && !shutdown.isInitiatedByClient();
        }));
    }

    /**
     * If the underlying reactor throws a HandlerException, we shut this down.
     */
    @Test
    public void closesOnHandlerException() throws InterruptedException {
        // Arrange
        final ReactorExecutor executor = new ReactorExecutor(reactor, scheduler, CONNECTION_ID, exceptionHandler,
            TIMEOUT, HOSTNAME);
        final Semaphore semaphore = new Semaphore(1);
        semaphore.acquire();

        final HandlerException exception = new HandlerException(mock(Handler.class),
            new UnsupportedOperationException("test-exception"));

        doAnswer(invocation -> {
            final Runnable work = invocation.getArgument(0);
            work.run();
            return null;
        }).when(scheduler).schedule(any(Runnable.class));

        // This is invoked when we schedule pending tasks before closing serializer.
        doAnswer(invocation -> {
            final Runnable work = invocation.getArgument(0);
            work.run();
            semaphore.release();
            return null;
        }).when(scheduler).schedule(any(Runnable.class), eq(TIMEOUT.toMillis()), eq(TimeUnit.MILLISECONDS));

        final AtomicInteger timesProcessed = new AtomicInteger();
        doAnswer(invocation -> {
            if (timesProcessed.getAndIncrement() == 0) {
                throw exception;
            } else {
                return false;
            }
        }).when(reactor).process();

        // Act
        executor.start();

        final boolean acquired = semaphore.tryAcquire(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        assertTrue(acquired, "Unable to stop reactor when there are no more items to process.");

        // Assert
        verify(reactor).start();
        verify(reactor).stop();
        verify(reactor).free();

        // 1st time: exception in run();
        // 3rd time: schedulePendingTasks
        verify(reactor, times(2)).process();
        verify(scheduler).dispose();

        verify(exceptionHandler).onConnectionError(argThat(error -> {
            return error instanceof AmqpException && ((AmqpException) error).isTransient();
        }));
        verify(exceptionHandler).onConnectionShutdown(argThat(shutdown -> {
            return !shutdown.isTransient() && !shutdown.isInitiatedByClient();
        }));
    }

    /**
     * If the scheduler is disposed, we cannot keep queueing work on it and it shuts down.
     */
    @Test
    public void closesOnRejectedExecutionException() throws InterruptedException {
        // Arrange
        final Record record = mock(Record.class);
        when(reactor.attachments()).thenReturn(record);

        final ReactorExecutor executor = new ReactorExecutor(reactor, scheduler, CONNECTION_ID, exceptionHandler,
            TIMEOUT, HOSTNAME);

        final Semaphore semaphore = new Semaphore(1);
        semaphore.acquire();

        final AtomicBoolean firstInvocation = new AtomicBoolean(true);
        final RejectedExecutionException exception = new RejectedExecutionException("test-rejection");
        doAnswer(invocation -> {
            if (firstInvocation.getAndSet(false)) {
                final Runnable work = invocation.getArgument(0);
                work.run();
                return null;
            } else {
                throw exception;
            }
        }).when(scheduler).schedule(any(Runnable.class));

        // This is invoked when we schedule pending tasks before closing serializer.
        doAnswer(invocation -> {
            final Runnable work = invocation.getArgument(0);
            work.run();
            semaphore.release();
            return null;
        }).when(scheduler).schedule(any(Runnable.class), eq(TIMEOUT.toMillis()), eq(TimeUnit.MILLISECONDS));

        final AtomicInteger timesProcessed = new AtomicInteger();
        doAnswer(invocation -> timesProcessed.getAndIncrement() == 0)
            .when(reactor).process();

        // Act
        executor.start();

        final boolean acquired = semaphore.tryAcquire(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        assertTrue(acquired, "Unable to stop reactor when there are no more items to process.");

        // Assert
        verify(reactor).start();
        verify(reactor).stop();
        verify(reactor).free();

        // 1st time: exception in run();
        // 3rd time: schedulePendingTasks
        verify(reactor, times(2)).process();
        verify(scheduler).dispose();

        verify(record).set(eq(RejectedExecutionException.class), eq(RejectedExecutionException.class), eq(exception));
    }

    /**
     * Reschedules the ReactorExecutor run method until not returned has occurred. But when scheduling pending tasks,
     * an exception is thrown from the scheduler (it could have been disposed). We expect it still closes.
     */
    @Test
    public void reschedulesSuccessfullyButFailsSchedulingClosingTasks() throws InterruptedException {
        // Arrange
        final ReactorExecutor executor = new ReactorExecutor(reactor, scheduler, CONNECTION_ID, exceptionHandler,
            TIMEOUT, HOSTNAME);
        final Semaphore semaphore = new Semaphore(1);
        semaphore.acquire();

        doAnswer(invocation -> {
            final Runnable work = invocation.getArgument(0);
            work.run();
            return null;
        }).when(scheduler).schedule(any(Runnable.class));

        // This is invoked when we schedule pending tasks before closing serializer.
        final RejectedExecutionException exception = new RejectedExecutionException("test-rejection");
        doAnswer(invocation -> {
            semaphore.release();
            throw exception;
        }).when(scheduler).schedule(any(Runnable.class), eq(TIMEOUT.toMillis()), eq(TimeUnit.MILLISECONDS));

        final AtomicInteger timesProcessed = new AtomicInteger();
        doAnswer(invocation -> timesProcessed.getAndIncrement() < 1)
            .when(reactor).process();

        // Act
        executor.start();

        final boolean acquired = semaphore.tryAcquire(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        assertTrue(acquired, "Unable to stop reactor when there are no more items to process.");

        // Assert
        verify(reactor).start();
        verify(reactor).stop();
        verify(reactor).free();

        // 1st time: success in run();
        // 2nd time: returns false in run()
        // 3rd time: schedulePendingTasks()
        verify(reactor, times(3)).process();
        verify(scheduler).dispose();

        verify(exceptionHandler).onConnectionShutdown(argThat(shutdown -> {
            return !shutdown.isTransient() && !shutdown.isInitiatedByClient();
        }));
    }

}
