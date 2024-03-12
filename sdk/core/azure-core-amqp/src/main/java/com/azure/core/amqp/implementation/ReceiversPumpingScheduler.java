// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * The common internal {@link Scheduler} instance that all {@link ReactorReceiver} instances use to pump events.
 * <p>
 * The {@link ReceiversPumpingScheduler} is backed by a Reactor BoundedElastic Scheduler instance that dynamically
 * creates
 * a bounded number {@code poolMaxSize} of single-threaded ExecutorService instances and pools them.
 * An abstraction named {@link Worker} front ends such a pooled ExecutorService, exposing API to schedule tasks to
 * ExecutorService instance it is associated with.
 * </p>
 * <p>
 * Each {@link ReactorReceiver} creates a Worker instance using the publishOn operator. The publishOn operator obtains
 * Worker using {@link ReceiversPumpingScheduler#createWorker()}. A Worker instance is pinned to a ReactorReceiver
 * as long as the receiver is active, i.e., until the receiver terminates. Termination of receiver disposes of its
 * Worker.
 * </p>
 * <p>
 * If there is a need for a Worker when the BoundedElastic pool has {@code poolMaxSize} the number of ExecutorService
 * instances,
 * the new Worker is assigned to a pooled ExecutorService instance with the least number of Workers. A pooled
 * ExecutorService
 * is considered idle once all Workers associated with it are disposed of; if no new Workers are created and associated
 * with
 * it within 60 seconds since idle, the ExecutorService (and its backing single-thread) is evicted from the pool.
 * </p>
 * <p>
 * The typical allocation for Java App in a containerized environment is 2-4 cores per node.
 * See <a href="https://learn.microsoft.com/azure/developer/java/containers/overview">Java containerization</a>.
 * For message processing, a 'setup' starting with 2-4 cores per node and replicating the nodes if needed is
 * recommended.
 * Typical, observed 'use-cases' are 1 to ~5 active {@link ReactorReceiver} (with a max-concurrent-calls per receiver in
 * ProcessorClient). The default {@code poolMaxSize} is 20 *count(cpu) and serves well in common 'setup' and 'use-cases'
 * or beyond. Under common 'setup' and 'use-cases', this can lead an arrangement of one Thread in the pool dedicated to
 * one {@link ReactorReceiver} instance.
 * </p>
 * <ol>
 * <li>If pool size tuning is really needed for a 'setup' and 'use-case', the default {@code poolMaxSize} can be
 * overridden
 * through the system property 'com.azure.core.amqp.receiversPumpingThreadPoolMaxSize'. There is no one-size-fits-all
 * guidance
 * for resource allocation. Resourcing depends on the nature of application work, other executor service (e.g. for DB,
 * REST calls)
 * resources in the application, load on these resources etc... and should be evaluated case by case.</li>
 * <li>We didn't want the pool to scale unbounded fashion like
 * {@link java.util.concurrent.Executors#newCachedThreadPool()}
 * for the same reasons that Reactor phased out the (unbounded) elastic Scheduler, and the recommendation from async
 * experts
 * is to use bounded pool.
 * See <a href="https://github.com/reactor/reactor-core/issues/1804#issuecomment-532626201">Unbounded Scheduler
 * removal</a>.
 * This is also the reason we don't want to create one Scheduler per {@link ReactorReceiver} instance. Since such an
 * approach
 * may result in certain application pattern to cause unbounded thread allocations leading to OOM.</li>
 * <li>The max-concurrent-calls functionality in ProcessorClient uses its own Scheduler to deliver the messages
 * concurrently.
 * Overall the threading arrangement is close to T1 (e.g., T1 Service Bus library), where a shared internal pool
 * (similar to
 * ReceiversPumpingScheduler) pumps the messages internally and different pool (customizable in T1) pump for
 * max-concurrent-calls.</li>
 * <li>The idle timeout of '60-sec' for the pool and the queue size of '100000' for each Java single-threaded
 * ExecutorService
 * in the pool are uplifted from the Reactor's choice for the same attributes.</li>
 * </ol>
 */
public final class ReceiversPumpingScheduler implements Scheduler {
    private static final ClientLogger LOGGER = new ClientLogger(ReceiversPumpingScheduler.class);
    private static final String NAME = "receiverPump";
    private static final int TASK_QUEUE_CAP = 100000;
    private static final int IDLE_TTL_SECONDS = 60;
    private static final AtomicReference<ReceiversPumpingScheduler> INSTANCE = new AtomicReference<>();
    private final Scheduler inner;

    /**
     * Obtain the common Scheduler.
     *
     * @return the Scheduler.
     */
    public static Scheduler instance() {
        if (isVTSchedulerMode()) {
            return Schedulers.boundedElastic();
        }
        ReceiversPumpingScheduler scheduler = INSTANCE.get();
        if (scheduler != null) {
            return scheduler;
        } else {
            scheduler = new ReceiversPumpingScheduler();
            if (!INSTANCE.compareAndSet(null, scheduler)) {
                scheduler.inner.dispose();
            }
            return INSTANCE.get();
        }
    }

    @Override
    public Disposable schedule(Runnable task) {
        return inner.schedule(task);
    }

    @Override
    public Disposable schedule(Runnable task, long delay, TimeUnit unit) {
        return inner.schedule(task, delay, unit);
    }

    @Override
    public Disposable schedulePeriodically(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return inner.schedulePeriodically(task, initialDelay, period, unit);
    }

    @Override
    public Worker createWorker() {
        return inner.createWorker();
    }

    @Override
    public long now(TimeUnit unit) {
        return inner.now(unit);
    }

    /**
     *  @deprecated replaced by init.
     */
    @Deprecated
    @Override
    public void start() {
        inner.start();
    }

    @Override
    public void init() {
        inner.init();
    }

    @Override
    public void dispose() {
        // NOP as the Scheduler INSTANCE is common.
    }

    @Override
    public boolean isDisposed() {
        return inner.isDisposed();
    }

    @Override
    public String toString() {
        return NAME;
    }

    private ReceiversPumpingScheduler() {
        final Supplier<Integer> poolMaxSizeDefault = () -> 20 * Runtime.getRuntime().availableProcessors();
        // Note: It would be nice to read the custom pool size using
        // com.azure.core.util.Configuration.getGlobalConfiguration.
        // It requires adding the key to the azure-core known configuration properties, let's evaluate it separately.
        final Optional<Integer> poolMaxSizeOverridden
            = Optional.ofNullable(System.getProperty("com.azure.core.amqp.receiversPumpingThreadPoolMaxSize"))
                .map(m -> {
                    try {
                        return Integer.parseInt(m);
                    } catch (NumberFormatException ignored) {
                        // Use poolMaxSizeDefault (the initialization log below hints the size is chosen).
                        return null;
                    }
                });
        final int poolMaxSize = poolMaxSizeOverridden.orElseGet(poolMaxSizeDefault);
        this.inner = Schedulers.newBoundedElastic(poolMaxSize, TASK_QUEUE_CAP, NAME, IDLE_TTL_SECONDS, true);
        LOGGER.atVerbose()
            .addKeyValue("maxThreads", poolMaxSize)
            .log("Initialized common thread pool for receivers pumping.");
    }

    /**
     * Check any of the Reactor's common (cached) Scheduler to see if "VirtualTimeScheduler" (VTScheduler) is injected;
     * if so, ReceiversPumpingScheduler won't create its backing Scheduler and cache it in INSTANCE.
     * <p>
     * Problem: When running under 'StepVerifier.withVirtualTime', the Reactor-test infra reset the "Schedulers" factory
     * such that VirtualTimeScheduler is returned from all Schedulers.new** APIs and from APIs to obtain common (cached)
     * Scheduler instances. As part of the reset, the Reactor also internally clears its common (cached) Scheduler
     * instances so that the next cache load loads VirtualTimeScheduler. Unfortunately, Reactor is not providing
     * any hooks for such reset so that the application caching its own Scheduler can clear its cache (INSTANCE static
     * member in ReceiversPumpingScheduler case) on reset. It leads to the problem of
     * </p>
     * <ul>
     *     <li>a test_1 using 'StepVerifier.create' populating the INSTANCE with a real Scheduler S and a following
     *     test_2 using 'StepVerifier.withVirtualTime' end up using S rather than loading and using VirtualTimeScheduler.</li>
     *     <li>a test_1 using 'StepVerifier.withVirtualTime' loads INSTANCE with VirtualTimeScheduler, dispose it
     *     at the end of test_2 execution, and a following test_2 using 'StepVerifier.withVirtualTime' uses the old cached
     *     disposed VirtualTimeScheduler.</li>
     * </ul>
     *
     * @return {@code true} if the VirtualTimeScheduler is loaded, {@code false} otherwise.
     */
    private static boolean isVTSchedulerMode() {
        // Is there a better way to check VTScheduler? / should we request the Reactor team for an API to detect it?
        //
        final Scheduler s = Schedulers.boundedElastic();
        if (s instanceof Supplier<?> && ((Supplier<?>) s).get() instanceof Scheduler) {
            return ((Supplier<?>) s).get().getClass().getSimpleName().equals("VirtualTimeScheduler");
        } else {
            LOGGER.atVerbose().log("Can't perform VTScheduler check.");
            return false;
        }
    }
}
