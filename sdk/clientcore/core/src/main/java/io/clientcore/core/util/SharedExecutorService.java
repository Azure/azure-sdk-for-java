// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util;

import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.util.EnvironmentConfiguration;
import io.clientcore.core.implementation.util.ImplUtils;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * An {@link ExecutorService} that is shared by multiple consumers.
 * <p>
 * The shared executor service is created using the following configuration settings:
 * <ul>
 *     <li>{@code clientcore.sdk.shared.threadpool.maxpoolsize} system property or
 *     {@code CLIENTCORE_SDK_SHARED_THREADPOOL_MAXPOOLSIZE} environment variable - The maximum pool size of the shared
 *     executor service. If not set, it defaults to 10 times the number of available processors.</li>
 *     <li>{@code clientcore.sdk.shared.threadpool.keepalivemillis} system property or
 *     {code CLIENTCORE_SDK_SHARED_THREADPOOL_KEEPALIVEMILLIS} environment variable - The keep alive time in millis for
 *     threads in the shared executor service. If not set, it defaults to 60 seconds. Limited to integer size.</li>
 *     <li>{@code clientcore.sdk.shared.threadpool.usevirtualthreads} system property or
 *     {@code CLIENTCORE_SDK_SHARED_THREADPOOL_USEVIRTUALTHREADS} environment variable - A boolean flag to indicate if
 *     the shared executor service should use virtual threads. If not set, it defaults to true. Ignored if virtual
 *     threads are not available in the runtime.</li>
 * </ul>
 */
@SuppressWarnings({ "resource", "NullableProblems" })
public final class SharedExecutorService implements ExecutorService {
    private static final ClientLogger LOGGER = new ClientLogger(SharedExecutorService.class);

    // Shared thread counter for all instances of SharedExecutorService created using the empty factory method.
    private static final AtomicLong CLIENTCORE_SDK_THREAD_COUNTER = new AtomicLong();
    private static final String CLIENTCORE_SDK_THREAD_NAME = "clientcore-sdk-global-thread-";

    // The thread pool size for the shared executor service.
    private static final int THREAD_POOL_SIZE;

    // The thread pool keep alive time for the shared executor service.
    private static final int THREAD_POOL_KEEP_ALIVE_MILLIS;

    // Virtual thread support for the shared executor service.
    private static final boolean THREAD_POOL_VIRTUAL;

    private static final SharedExecutorService INSTANCE;

    static {
        THREAD_POOL_SIZE
            = getConfig("clientcore.sdk.shared.threadpool.maxpoolsize", "CLIENTCORE_SDK_SHARED_THREADPOOL_MAXPOOLSIZE",
            Integer::parseInt, 10 * Runtime.getRuntime().availableProcessors());

        THREAD_POOL_KEEP_ALIVE_MILLIS = getConfig("clientcore.sdk.shared.threadpool.keepalivemillis",
            "CLIENTCORE_SDK_SHARED_THREADPOOL_KEEPALIVEMILLIS", Integer::parseInt, 60_000);

        THREAD_POOL_VIRTUAL = getConfig("clientcore.sdk.shared.threadpool.usevirtualthreads",
            "CLIENTCORE_SDK_SHARED_THREADPOOL_USEVIRTUALTHREADS", Boolean::parseBoolean, true);

        INSTANCE = new SharedExecutorService();
    }

    private static <T> T getConfig(String systemProperty, String envVar, Function<String, T> converter,
        T defaultValue) {
        String foundValue = ImplUtils.getFromEnvironment(EnvironmentConfiguration.getGlobalConfiguration(),
            systemProperty, envVar, ImplUtils.DEFAULT_SANITIZER, LOGGER);

        if (foundValue == null) {
            LOGGER.atVerbose()
                .addKeyValue("systemProperty", systemProperty)
                .addKeyValue("envVar", envVar)
                .addKeyValue("defaultValue", defaultValue)
                .log("Configuration value not found, using default.");
            return defaultValue;
        }

        try {
            // No need to log here as ImplUtils.getFromEnvironment logs the value.
            return converter.apply(foundValue);
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
                Class.forName("java.lang.Thread$Builder").getDeclaredMethod("name", String.class));
            createVirtualThreadFactory = ReflectionUtils.getMethodInvoker(null,
                Class.forName("java.lang.Thread$Builder").getDeclaredMethod("factory"));
            virtualThreadSupported = true;
            LOGGER.atVerbose().log("Virtual threads are supported in the current runtime.");
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

    private final ExecutorService executorService;

    private SharedExecutorService() {
        this.executorService = createSharedExecutor();
    }

    /**
     * Gets the shared instance of the executor service.
     *
     * @return The shared instance of the executor service.
     */
    public static SharedExecutorService getInstance() {
        return INSTANCE;
    }

    //    /**
    //     * Sets the backing executor service for the shared instance.
    //     * <p>
    //     * This updates the executor service for all users of the {@link #getInstance() shared instance}. Meaning, if
    //     * another area in code already had a reference to the shared instance, it will now use the passed executor service
    //     * to execute tasks.
    //     * <p>
    //     * If the executor service is already set, this will replace it with the new executor service. If the replaced
    //     * executor service was created by this class, it will be shut down.
    //     * <p>
    //     * If the passed executor service is null, this will throw a {@link NullPointerException}. If the passed executor
    //     *
    //     * @param executorService The executor service to set as the shared instance.
    //     * @throws NullPointerException If the passed executor service is null.
    //     * @throws IllegalStateException If the passed executor service is shutdown or terminated.
    //     */
    //    public static void setExecutorService(ExecutorService executorService) {
    //        // We allow for the global executor service to be set from an external source to allow for consumers of the SDK
    //        // to use their own thread management to run ClientCore SDK tasks. This allows for the SDKs to perform deeper
    //        // integration into an environment, such as the consumer environment knowing details about capacity, allowing
    //        // the custom executor service to better manage resources than our more general 10x the number of processors.
    //        // Another scenario could be an executor service that creates threads with specific permissions, such as
    //        // allowing to perform deep reflection on classes that are not normally allowed.
    //        Objects.requireNonNull(executorService, "'executorService' cannot be null.");
    //        if (executorService.isShutdown() || executorService.isTerminated()) {
    //            throw new IllegalStateException("The passed executor service is shutdown or terminated.");
    //        }
    //
    //        ExecutorWithMetadata existing
    //            = INSTANCE.wrappedExecutorService.getAndSet(new ExecutorWithMetadata(executorService, null));
    //
    //        if (existing != null) {
    //            // This is calling ExecutorWithMetadata.shutdown() which will shutdown the executor service if it was
    //            // created by this class. Otherwise, it's a no-op.
    //            existing.shutdown();
    //        }
    //    }

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

    private ExecutorService ensureNotShutdown() {
        return executorService;
        //        return wrappedExecutorService.updateAndGet(wrapper -> {
        //            if (wrapper == null || wrapper.executorService.isShutdown() || wrapper.executorService.isTerminated()) {
        //                return createSharedExecutor();
        //            } else {
        //                return wrapper;
        //            }
        //        }).executorService;
    }

    private static ExecutorService createSharedExecutor() {
        ThreadFactory threadFactory;
        if (VIRTUAL_THREAD_SUPPORTED && THREAD_POOL_VIRTUAL) {
            try {
                LOGGER.atVerbose().log("Attempting to create a virtual thread factory.");
                threadFactory = createVirtualThreadFactory();
                LOGGER.atVerbose().log("Successfully created a virtual thread factory.");
            } catch (Exception e) {
                LOGGER.atInfo()
                    .log("Failed to create a virtual thread factory, falling back to non-virtual threads.", e);
                threadFactory = createNonVirtualThreadFactory();
            }
        } else {
            threadFactory = createNonVirtualThreadFactory();
        }

        ExecutorService executorService = new ThreadPoolExecutor(0, THREAD_POOL_SIZE, THREAD_POOL_KEEP_ALIVE_MILLIS,
            TimeUnit.MILLISECONDS, new SynchronousQueue<>(), threadFactory);
        Thread shutdownThread = ImplUtils.createExecutorServiceShutdownThread(executorService, Duration.ofSeconds(5));
        ImplUtils.addShutdownHookSafely(shutdownThread);

        return executorService;
    }

    private static ThreadFactory createVirtualThreadFactory() throws Exception {
        Object virtualThreadBuilder = GET_VIRTUAL_THREAD_BUILDER.invokeStatic();
        SET_VIRTUAL_THREAD_BUILDER_THREAD_NAME.invokeWithArguments(virtualThreadBuilder, CLIENTCORE_SDK_THREAD_NAME);
        return (ThreadFactory) CREATE_VIRTUAL_THREAD_FACTORY.invokeWithArguments(virtualThreadBuilder);
    }

    private static ThreadFactory createNonVirtualThreadFactory() {
        return r -> {
            Thread thread = new Thread(r, CLIENTCORE_SDK_THREAD_NAME + CLIENTCORE_SDK_THREAD_COUNTER.getAndIncrement());
            thread.setDaemon(true);

            return thread;
        };
    }

    //    private static final class ExecutorWithMetadata {
    //        private final Thread shutdownThread;
    //        private final ExecutorService executorService;
    //
    //        ExecutorWithMetadata(ExecutorService executorService, Thread shutdownThread) {
    //            this.executorService = executorService;
    //            this.shutdownThread = shutdownThread;
    //        }
    //
    //        void shutdown() {
    //            // The executor service is only shutdown if there is a shutdown thread as that indicates the executor
    //            // service was created by this class.
    //            if (shutdownThread != null) {
    //                executorService.shutdown();
    //                ImplUtils.removeShutdownHookSafely(shutdownThread);
    //            }
    //        }
    //    }
}
