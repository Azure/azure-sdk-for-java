// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.implementation.ImplUtils;
import com.azure.core.implementation.ReflectionUtils;
import com.azure.core.implementation.ReflectiveInvoker;
import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;

/**
 * An {@link ScheduledExecutorService} that is shared by multiple consumers.
 * <p>
 * If {@link SharedExecutorService#setExecutorService(ScheduledExecutorService)} isn't called a default shared executor
 * service is created using the following configuration settings:
 * <ul>
 *     <li>{@code azure.sdk.shared.threadpool.maxpoolsize} system property or
 *     {@code AZURE_SDK_SHARED_THREADPOOL_MAXPOOLSIZE} environment variable - The maximum pool size of the shared
 *     executor service. If not set, it defaults to 10 times the number of available processors.</li>
 *     <li>{@code azure.sdk.shared.threadpool.keepalivemillis} system property or
 *     {code AZURE_SDK_SHARED_THREADPOOL_KEEPALIVEMILLIS} environment variable - The keep alive time in millis for
 *     threads in the shared executor service. If not set, it defaults to 60 seconds. Limited to integer size.</li>
 *     <li>{@code azure.sdk.shared.threadpool.usevirtualthreads} system property or
 *     {@code AZURE_SDK_SHARED_THREADPOOL_USEVIRTUALTHREADS} environment variable - A boolean flag to indicate if the
 *     shared executor service should use virtual threads. If not set, it defaults to true. Ignored if virtual threads
 *     are not available in the runtime.</li>
 * </ul>
 */
@SuppressWarnings({ "resource", "NullableProblems" })
public final class SharedExecutorService implements ScheduledExecutorService {
    private static final ClientLogger LOGGER = new ClientLogger(SharedExecutorService.class);

    // Shared thread counter for all instances of SharedExecutorService created using the empty factory method.
    private static final AtomicLong AZURE_SDK_THREAD_COUNTER = new AtomicLong();
    private static final String AZURE_SDK_THREAD_NAME = "azure-sdk-global-thread-";

    // The thread pool size for the shared executor service.
    private static final int THREAD_POOL_SIZE;

    // The thread pool keep alive time for the shared executor service.
    private static final int THREAD_POOL_KEEP_ALIVE_MILLIS;

    // Virtual thread support for the shared executor service.
    private static final boolean THREAD_POOL_VIRTUAL;

    private static final SharedExecutorService INSTANCE;

    static {
        THREAD_POOL_SIZE
            = getConfig("azure.sdk.shared.threadpool.maxpoolsize", "AZURE_SDK_SHARED_THREADPOOL_MAXPOOLSIZE",
                Integer::parseInt, 10 * Runtime.getRuntime().availableProcessors());

        THREAD_POOL_KEEP_ALIVE_MILLIS = getConfig("azure.sdk.shared.threadpool.keepalivemillis",
            "AZURE_SDK_SHARED_THREADPOOL_KEEPALIVEMILLIS", Integer::parseInt, 60_000);

        THREAD_POOL_VIRTUAL = getConfig("azure.sdk.shared.threadpool.usevirtualthreads",
            "AZURE_SDK_SHARED_THREADPOOL_USEVIRTUALTHREADS", Boolean::parseBoolean, true);

        INSTANCE = new SharedExecutorService();
    }

    private static <T> T getConfig(String systemProperty, String envVar, Function<String, T> converter,
        T defaultValue) {
        String foundValue = Configuration.getGlobalConfiguration()
            .getFromEnvironment(systemProperty, envVar, ConfigurationProperty.REDACT_VALUE_SANITIZER);
        if (foundValue == null) {
            LOGGER.atVerbose()
                .addKeyValue("systemProperty", systemProperty)
                .addKeyValue("envVar", envVar)
                .addKeyValue("defaultValue", defaultValue)
                .log("Configuration value not found, using default.");
            return defaultValue;
        }

        try {
            T returnValue = converter.apply(foundValue);
            LOGGER.atVerbose()
                .addKeyValue("systemProperty", systemProperty)
                .addKeyValue("envVar", envVar)
                .addKeyValue("value", foundValue)
                .log("Found configuration value.");
            return returnValue;
        } catch (RuntimeException e) {
            LOGGER.atVerbose()
                .addKeyValue("systemProperty", systemProperty)
                .addKeyValue("envVar", envVar)
                .addKeyValue("value", foundValue)
                .addKeyValue("defaultValue", defaultValue)
                .log("Failed to convert found configuration value, using default.");
            return defaultValue;
        }
    }

    private static final boolean VIRTUAL_THREAD_SUPPORTED;
    private static final ReflectiveInvoker GET_VIRTUAL_THREAD_BUILDER;
    private static final ReflectiveInvoker SET_VIRTUAL_THREAD_BUILDER_THREAD_NAME;
    private static final ReflectiveInvoker CREATE_VIRTUAL_THREAD_FACTORY;

    static {
        boolean virtualThreadSupported;
        ReflectiveInvoker getVirtualThreadBuilder;
        ReflectiveInvoker setVirtualThreadBuilderThreadName;
        ReflectiveInvoker createVirtualThreadFactory;

        try {
            getVirtualThreadBuilder = ReflectionUtils.getMethodInvoker(null,
                Class.forName("java.lang.Thread").getDeclaredMethod("ofVirtual"));
            setVirtualThreadBuilderThreadName = ReflectionUtils.getMethodInvoker(null,
                Class.forName("java.lang.Thread$Builder").getDeclaredMethod("name", String.class, long.class));
            createVirtualThreadFactory = ReflectionUtils.getMethodInvoker(null,
                Class.forName("java.lang.Thread$Builder").getDeclaredMethod("factory"));
            virtualThreadSupported = true;
            LOGGER.verbose("Virtual threads are supported in the current runtime.");
        } catch (Exception | LinkageError e) {
            LOGGER.atVerbose()
                .addKeyValue("runtime", System.getProperty("java.version"))
                .log("Virtual threads are not supported in the current runtime.", e);
            virtualThreadSupported = false;
            getVirtualThreadBuilder = null;
            setVirtualThreadBuilderThreadName = null;
            createVirtualThreadFactory = null;
        }

        VIRTUAL_THREAD_SUPPORTED = virtualThreadSupported;
        GET_VIRTUAL_THREAD_BUILDER = getVirtualThreadBuilder;
        SET_VIRTUAL_THREAD_BUILDER_THREAD_NAME = setVirtualThreadBuilderThreadName;
        CREATE_VIRTUAL_THREAD_FACTORY = createVirtualThreadFactory;
    }

    volatile ScheduledExecutorService executor;
    private static final AtomicReferenceFieldUpdater<SharedExecutorService, ScheduledExecutorService> EXECUTOR_UPDATER
        = AtomicReferenceFieldUpdater.newUpdater(SharedExecutorService.class, ScheduledExecutorService.class,
            "executor");

    private SharedExecutorService() {
    }

    /**
     * Gets the shared instance of the executor service.
     *
     * @return The shared instance of the executor service.
     */
    public static SharedExecutorService getInstance() {
        return INSTANCE;
    }

    /**
     * Gets the backing executor service for the shared instance.
     * <p>
     * This returns the executor service for all users of the {@link #getInstance() shared instance}. Meaning, if
     * another area in code already had a reference to the shared instance.
     * <p>
     * This may return null if the shared instance has not been set yet.
     *
     * @return The executor service that is set as the shared instance, may be null if a shared instance hasn't been
     * set.
     */
    public ScheduledExecutorService getExecutorService() {
        return EXECUTOR_UPDATER.get(this);
    }

    /**
     * Sets the backing executor service for the shared instance.
     * <p>
     * This updates the executor service for all users of the {@link #getInstance() shared instance}. Meaning, if
     * another area in code already had a reference to the shared instance, it will now use the passed executor service
     * to execute tasks.
     * <p>
     * If the executor service is already set, this will replace it with the new executor service. If the replaced
     * executor service was created by this class, it will be shut down.
     * <p>
     * If the passed executor service is null, this will throw a {@link NullPointerException}. If the passed executor
     * service is shutdown or terminated, this will throw an {@link IllegalStateException}.
     *
     * @param executorService The executor service to set as the shared instance.
     * @throws NullPointerException If the passed executor service is null.
     * @throws IllegalStateException If the passed executor service is shutdown or terminated.
     */
    public void setExecutorService(ScheduledExecutorService executorService) {
        // We allow for the global executor service to be set from an external source to allow for consumers of the SDK
        // to use their own thread management to run Azure SDK tasks. This allows for the SDKs to perform deeper
        // integration into an environment, such as the consumer environment knowing details about capacity, allowing
        // the custom executor service to better manage resources than our more general 10x the number of processors.
        // Another scenario could be an executor service that creates threads with specific permissions, such as
        // allowing Azure Core or Jackson to perform deep reflection on classes that are not normally allowed.
        Objects.requireNonNull(executorService, "'executorService' cannot be null.");
        if (executorService.isShutdown() || executorService.isTerminated()) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("The passed executor service is shutdown or terminated."));
        }

        ExecutorService existing = EXECUTOR_UPDATER.getAndSet(this, executorService);

        if (existing instanceof InternalExecutorService) {
            // Only the InternalExecutorService should be shut down when setting a new ExecutorService.
            existing.shutdown();
        }
    }

    /**
     * Resets the state of the {@link #getInstance()} to an uninitialized state.
     * <p>
     * This will shut down the executor service if it was created by this class.
     */
    public void reset() {
        ScheduledExecutorService existing = EXECUTOR_UPDATER.getAndSet(this, null);

        if (existing instanceof InternalExecutorService) {
            // Only the InternalExecutorService should be shut down when resetting SharedExecutorService.
            existing.shutdown();
        }
    }

    /**
     * Shutdown isn't supported for this executor service as it is shared by multiple consumers.
     * <p>
     * Calling this method will result in an {@link UnsupportedOperationException} being thrown.
     *
     * @throws UnsupportedOperationException This method will always throw an exception.
     */
    @Override
    public void shutdown() {
        // This doesn't do anything as this is meant to be shared and shouldn't be shut down by one consumer.
        throw LOGGER.logThrowableAsError(
            new UnsupportedOperationException("This executor service is shared and cannot be shut down."));
    }

    /**
     * Shutdown isn't supported for this executor service as it is shared by multiple consumers.
     * <p>
     * Calling this method will result in an {@link UnsupportedOperationException} being thrown.
     *
     * @return Nothing will be returned as an exception will always be thrown.
     * @throws UnsupportedOperationException This method will always throw an exception.
     */
    @Override
    public List<Runnable> shutdownNow() {
        throw LOGGER.logThrowableAsError(
            new UnsupportedOperationException("This executor service is shared and cannot be shut down."));
    }

    /**
     * Checks if the executor service is shutdown.
     * <p>
     * Will always return false as the shared executor service cannot be shut down.
     *
     * @return False, as the shared executor service cannot be shut down.
     */
    @Override
    public boolean isShutdown() {
        return false;
    }

    /**
     * Checks if the executor service is terminated.
     * <p>
     * Will always return false as the shared executor service cannot be terminated.
     *
     * @return False, as the shared executor service cannot be terminated.
     */
    @Override
    public boolean isTerminated() {
        return false;
    }

    /**
     * Shutdown isn't supported for this executor service as it is shared by multiple consumers.
     * <p>
     * Calling this method will result in an {@link UnsupportedOperationException} being thrown.
     *
     * @param timeout The amount of time to wait for the executor service to shutdown.
     * @param unit The unit of time for the timeout.
     * @return Nothing will be returned as an exception will always be thrown.
     * @throws UnsupportedOperationException This method will always throw an exception.
     */
    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
        throw LOGGER.logThrowableAsError(
            new UnsupportedOperationException("This executor service is shared and cannot be terminated."));
    }

    @Override
    public void execute(Runnable command) {
        ensureNotShutdown().execute(command);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return ensureNotShutdown().submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return ensureNotShutdown().submit(task, result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return ensureNotShutdown().submit(task);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return ensureNotShutdown().invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
        throws InterruptedException {
        return ensureNotShutdown().invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return ensureNotShutdown().invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        return ensureNotShutdown().invokeAny(tasks, timeout, unit);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return ensureNotShutdown().schedule(command, delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return ensureNotShutdown().schedule(callable, delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return ensureNotShutdown().scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return ensureNotShutdown().scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    private ScheduledExecutorService ensureNotShutdown() {
        return EXECUTOR_UPDATER.updateAndGet(INSTANCE,
            ex -> (ex == null || ex.isShutdown() || ex.isTerminated()) ? createSharedExecutor() : ex);
    }

    private static ScheduledExecutorService createSharedExecutor() {
        ThreadFactory threadFactory;
        if (VIRTUAL_THREAD_SUPPORTED && THREAD_POOL_VIRTUAL) {
            try {
                LOGGER.verbose("Attempting to create a virtual thread factory.");
                threadFactory = createVirtualThreadFactory();
                LOGGER.verbose("Successfully created a virtual thread factory.");
            } catch (Exception e) {
                LOGGER.info("Failed to create a virtual thread factory, falling back to non-virtual threads.", e);
                threadFactory = createNonVirtualThreadFactory();
            }
        } else {
            threadFactory = createNonVirtualThreadFactory();
        }

        ScheduledThreadPoolExecutor executorService = new ScheduledThreadPoolExecutor(THREAD_POOL_SIZE, threadFactory);
        executorService.setKeepAliveTime(THREAD_POOL_KEEP_ALIVE_MILLIS, TimeUnit.MILLISECONDS);
        Thread shutdownThread = CoreUtils.createExecutorServiceShutdownThread(executorService, Duration.ofSeconds(5));
        CoreUtils.addShutdownHookSafely(shutdownThread);

        return new InternalExecutorService(executorService, shutdownThread);
    }

    private static ThreadFactory createVirtualThreadFactory() throws Exception {
        Object virtualThreadBuilder = GET_VIRTUAL_THREAD_BUILDER.invokeStatic();
        SET_VIRTUAL_THREAD_BUILDER_THREAD_NAME.invokeWithArguments(virtualThreadBuilder, AZURE_SDK_THREAD_NAME,
            AZURE_SDK_THREAD_COUNTER.get());
        ThreadFactory virtual = (ThreadFactory) CREATE_VIRTUAL_THREAD_FACTORY.invokeWithArguments(virtualThreadBuilder);
        return r -> {
            AZURE_SDK_THREAD_COUNTER.incrementAndGet();
            return virtual.newThread(r);
        };
    }

    private static ThreadFactory createNonVirtualThreadFactory() {
        return r -> {
            Thread thread = new Thread(r, AZURE_SDK_THREAD_NAME + AZURE_SDK_THREAD_COUNTER.getAndIncrement());
            thread.setDaemon(true);

            return thread;
        };
    }

    static final class InternalExecutorService implements ScheduledExecutorService {
        private final ScheduledExecutorService wrapped;
        private final Thread shutdownThread;

        private InternalExecutorService(ScheduledExecutorService wrapped, Thread shutdownThread) {
            this.wrapped = wrapped;
            this.shutdownThread = shutdownThread;
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
            return wrapped.invokeAny(tasks, timeout, unit);
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
            return wrapped.invokeAny(tasks);
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
            return wrapped.invokeAll(tasks, timeout, unit);
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
            return wrapped.invokeAll(tasks);
        }

        @Override
        public Future<?> submit(Runnable task) {
            return wrapped.submit(task);
        }

        @Override
        public <T> Future<T> submit(Runnable task, T result) {
            return wrapped.submit(task, result);
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            return wrapped.submit(task);
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return wrapped.awaitTermination(timeout, unit);
        }

        @Override
        public boolean isTerminated() {
            return wrapped.isTerminated();
        }

        @Override
        public boolean isShutdown() {
            return wrapped.isShutdown();
        }

        @Override
        public List<Runnable> shutdownNow() {
            return wrapped.shutdownNow();
        }

        @Override
        public void shutdown() {
            wrapped.shutdown();
            ImplUtils.removeShutdownHookSafely(shutdownThread);
        }

        @Override
        public void execute(Runnable command) {
            wrapped.execute(command);
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            return wrapped.schedule(command, delay, unit);
        }

        @Override
        public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
            return wrapped.schedule(callable, delay, unit);
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
            return wrapped.scheduleAtFixedRate(command, initialDelay, period, unit);
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay,
            TimeUnit unit) {
            return wrapped.scheduleWithFixedDelay(command, initialDelay, delay, unit);
        }
    }
}
