// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
@Isolated("Modifies shared global state in SharedExecutorService")
public class SharedExecutorServiceConcurrencyTests {
    private final SharedExecutorService shared = SharedExecutorService.getInstance();
    private final ExecutorService callers = Executors.newFixedThreadPool(2);
    private final List<Candidate> candidates = new CopyOnWriteArrayList<>();
    private final List<ScheduledExecutorService> customExecutors = new ArrayList<>();

    @BeforeEach
    public void resetBeforeTesting() {
        shared.reset();
    }

    @AfterEach
    public void cleanup() throws InterruptedException {
        callers.shutdownNow();
        try {
            assertTrue(callers.awaitTermination(10, TimeUnit.SECONDS));
        } finally {
            shared.reset();
            for (Candidate candidate : candidates) {
                candidate.pool.shutdownNow();
                if (candidate.hook != null) {
                    Runtime.getRuntime().removeShutdownHook(candidate.hook);
                }
            }
            for (ScheduledExecutorService custom : customExecutors) {
                custom.shutdownNow();
            }
        }
    }

    @ParameterizedTest
    @EnumSource(InitialState.class)
    public void concurrentInitializationCreatesOneExecutor(InitialState initialState) throws Exception {
        for (int round = 0; round < 5; round++) {
            prepareInitialState(initialState);
            int firstCandidate = candidates.size();
            CountDownLatch created = new CountDownLatch(1);
            CountDownLatch publish = new CountDownLatch(1);
            Future<ScheduledExecutorService> first = callers.submit(() -> shared.ensureNotShutdown(() -> {
                Candidate candidate = createCandidate(true);
                created.countDown();
                await(publish);
                return candidate.service;
            }));
            Future<ScheduledExecutorService> second;

            try {
                await(created);
                AtomicReference<Thread> secondThread = new AtomicReference<>();
                second = callers.submit(() -> {
                    secondThread.set(Thread.currentThread());
                    return shared.ensureNotShutdown(() -> createCandidate(true).service);
                });
                awaitThreadWaiting(secondThread);
                assertFalse(second.isDone(), "The contending caller must wait for initialization.");
                assertEquals(firstCandidate + 1, candidates.size());
            } finally {
                publish.countDown();
            }
            ScheduledExecutorService executor = first.get(10, TimeUnit.SECONDS);
            assertSame(executor, second.get(10, TimeUnit.SECONDS));
            assertSame(executor, shared.getExecutorService());
            assertEquals(firstCandidate + 1, candidates.size());
            Candidate candidate = candidates.get(firstCandidate);
            assertSame(candidate.service, executor);
            assertFalse(candidate.pool.isShutdown());
            assertThrows(IllegalArgumentException.class, () -> Runtime.getRuntime().addShutdownHook(candidate.hook));

            shared.reset();
            assertCleanedUp(candidate);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void customExecutorWaitsForInitialization(boolean registerHook) throws Exception {
        CountDownLatch created = new CountDownLatch(1);
        CountDownLatch publish = new CountDownLatch(1);
        Future<ScheduledExecutorService> result = callers.submit(() -> shared.ensureNotShutdown(() -> {
            Candidate candidate = createCandidate(registerHook);
            created.countDown();
            await(publish);
            return candidate.service;
        }));
        ScheduledExecutorService custom = createCustomExecutor();
        Future<?> replacement;

        try {
            await(created);
            AtomicReference<Thread> replacementThread = new AtomicReference<>();
            replacement = callers.submit(() -> {
                replacementThread.set(Thread.currentThread());
                shared.setExecutorService(custom);
            });
            awaitThreadWaiting(replacementThread);
            assertFalse(replacement.isDone(), "Replacement must wait for initialization.");
        } finally {
            publish.countDown();
        }

        assertSame(candidates.get(0).service, result.get(10, TimeUnit.SECONDS));
        replacement.get(10, TimeUnit.SECONDS);
        assertSame(custom, shared.getExecutorService());
        assertFalse(custom.isShutdown());
        assertEquals(1, candidates.size());
        assertCleanedUp(candidates.get(0));
    }

    @Test
    public void resetWaitsForInitialization() throws Exception {
        prepareInitialState(InitialState.SHUTDOWN);
        CountDownLatch created = new CountDownLatch(1);
        CountDownLatch publish = new CountDownLatch(1);
        Future<ScheduledExecutorService> result = callers.submit(() -> shared.ensureNotShutdown(() -> {
            Candidate candidate = createCandidate(true);
            created.countDown();
            await(publish);
            return candidate.service;
        }));
        Future<?> reset;

        try {
            await(created);
            AtomicReference<Thread> resetThread = new AtomicReference<>();
            reset = callers.submit(() -> {
                resetThread.set(Thread.currentThread());
                shared.reset();
            });
            awaitThreadWaiting(resetThread);
            assertFalse(reset.isDone(), "Reset must wait for initialization.");
        } finally {
            publish.countDown();
        }

        assertSame(candidates.get(0).service, result.get(10, TimeUnit.SECONDS));
        reset.get(10, TimeUnit.SECONDS);
        assertEquals(1, candidates.size());
        assertCleanedUp(candidates.get(0));
        assertNull(shared.getExecutorService());
        assertEquals("reinitialized", shared.submit(() -> "reinitialized").get(10, TimeUnit.SECONDS));
    }

    @Test
    public void customExecutorShutDownWhileWaitingIsRejected() throws Exception {
        CountDownLatch created = new CountDownLatch(1);
        CountDownLatch publish = new CountDownLatch(1);
        Future<ScheduledExecutorService> result = callers.submit(() -> shared.ensureNotShutdown(() -> {
            Candidate candidate = createCandidate(true);
            created.countDown();
            await(publish);
            return candidate.service;
        }));
        ScheduledExecutorService custom = createCustomExecutor();
        Future<?> replacement;

        try {
            await(created);
            AtomicReference<Thread> replacementThread = new AtomicReference<>();
            replacement = callers.submit(() -> {
                replacementThread.set(Thread.currentThread());
                shared.setExecutorService(custom);
            });
            awaitThreadWaiting(replacementThread);
            assertFalse(replacement.isDone(), "Replacement must wait for initialization.");
            custom.shutdown();
        } finally {
            publish.countDown();
        }

        ScheduledExecutorService executor = result.get(10, TimeUnit.SECONDS);
        ExecutionException failure
            = assertThrows(ExecutionException.class, () -> replacement.get(10, TimeUnit.SECONDS));
        assertInstanceOf(IllegalStateException.class, failure.getCause());
        assertSame(executor, shared.getExecutorService());
        assertFalse(executor.isShutdown());
    }

    @Test
    public void healthyExecutorDoesNotCreateCandidate() throws Exception {
        ScheduledExecutorService custom = createCustomExecutor();
        shared.setExecutorService(custom);

        assertSame(custom, shared.ensureNotShutdown(() -> {
            throw new AssertionError("A healthy executor must not create a replacement.");
        }));
        assertEquals("submitted", shared.submit(() -> "submitted").get(10, TimeUnit.SECONDS));
        assertEquals("scheduled", shared.schedule(() -> "scheduled", 0, TimeUnit.SECONDS).get(10, TimeUnit.SECONDS));
        assertFalse(custom.isShutdown());
    }

    @Test
    public void defaultExecutorSupportsSubmissionAndRecreation() throws Exception {
        assertNull(shared.getExecutorService());
        assertEquals("submitted", shared.submit(() -> "submitted").get(10, TimeUnit.SECONDS));
        ScheduledExecutorService original = shared.getExecutorService();
        assertInstanceOf(SharedExecutorService.InternalExecutorService.class, original);
        original.shutdown();

        assertEquals("scheduled", shared.schedule(() -> "scheduled", 0, TimeUnit.SECONDS).get(10, TimeUnit.SECONDS));
        assertNotSame(original, shared.getExecutorService());
        assertInstanceOf(SharedExecutorService.InternalExecutorService.class, shared.getExecutorService());
    }

    @Test
    public void customExecutorsRemainCallerOwned() {
        ScheduledExecutorService first = createCustomExecutor();
        ScheduledExecutorService second = createCustomExecutor();
        shared.setExecutorService(first);
        shared.setExecutorService(second);
        shared.reset();

        assertNull(shared.getExecutorService());
        assertFalse(first.isShutdown());
        assertFalse(second.isShutdown());
    }

    @Test
    public void replacingInternalExecutorRemovesItsHook() {
        Candidate candidate = createCandidate(true);
        assertSame(candidate.service, shared.ensureNotShutdown(() -> candidate.service));
        ScheduledExecutorService custom = createCustomExecutor();
        shared.setExecutorService(custom);

        assertSame(custom, shared.getExecutorService());
        assertFalse(custom.isShutdown());
        assertCleanedUp(candidate);
    }

    @Test
    public void factoryFailureReleasesLock() throws Exception {
        IllegalStateException failure = new IllegalStateException("Executor creation failed.");
        assertSame(failure, assertThrows(IllegalStateException.class, () -> shared.ensureNotShutdown(() -> {
            throw failure;
        })));
        assertNull(shared.getExecutorService());

        Candidate candidate = createCandidate(true);
        Future<ScheduledExecutorService> result
            = callers.submit(() -> shared.ensureNotShutdown(() -> candidate.service));
        assertSame(candidate.service, result.get(10, TimeUnit.SECONDS));
        assertFalse(candidate.pool.isShutdown());
    }

    private void prepareInitialState(InitialState initialState) {
        if (initialState == InitialState.UNINITIALIZED) {
            return;
        }
        TerminatedExecutor previous = new TerminatedExecutor();
        customExecutors.add(previous);
        shared.setExecutorService(previous);
        if (initialState == InitialState.SHUTDOWN) {
            previous.shutdown();
        } else {
            previous.terminated = true;
        }
    }

    private ScheduledExecutorService createCustomExecutor() {
        ScheduledExecutorService custom = Executors.newScheduledThreadPool(1);
        customExecutors.add(custom);
        return custom;
    }

    private Candidate createCandidate(boolean registerHook) {
        ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(1);
        Thread hook = registerHook ? new Thread(pool::shutdown) : null;
        Candidate candidate = new Candidate(pool, hook);
        candidates.add(candidate);
        if (hook != null) {
            Runtime.getRuntime().addShutdownHook(hook);
        }
        return candidate;
    }

    private static void assertCleanedUp(Candidate candidate) {
        assertAll(() -> assertTrue(candidate.pool.isShutdown(), "Replaced executor must be shut down."), () -> {
            if (candidate.hook != null) {
                assertFalse(Runtime.getRuntime().removeShutdownHook(candidate.hook),
                    "Replaced executor's hook must already be removed.");
            }
        });
    }

    private static void awaitThreadWaiting(AtomicReference<Thread> threadReference) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            Thread thread = threadReference.get();
            if (thread != null && thread.getState() == Thread.State.WAITING) {
                return;
            }
            Thread.yield();
        }
        throw new AssertionError("Thread did not wait for the executor lifecycle lock.");
    }

    private static void await(CountDownLatch latch) {
        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS), "Timed out coordinating executor creation.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while coordinating executor creation.", e);
        }
    }

    private enum InitialState {
        UNINITIALIZED, SHUTDOWN, TERMINATED
    }

    private static final class TerminatedExecutor extends ScheduledThreadPoolExecutor {
        private volatile boolean terminated;

        private TerminatedExecutor() {
            super(1);
        }

        @Override
        public boolean isTerminated() {
            return terminated || super.isTerminated();
        }
    }

    private static final class Candidate {
        private final ScheduledThreadPoolExecutor pool;
        private final Thread hook;
        private final SharedExecutorService.InternalExecutorService service;

        private Candidate(ScheduledThreadPoolExecutor pool, Thread hook) {
            this.pool = pool;
            this.hook = hook;
            this.service = new SharedExecutorService.InternalExecutorService(pool, hook);
        }
    }
}
