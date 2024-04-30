// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

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
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * An {@link ExecutorService} that is shared by multiple consumers.
 * <p>
 * A default shared executor service is created if one isn't set using {@link #setExecutorService(ExecutorService)}. The
 * shared executor service is created using the following configuration settings:
 * <ul>
 *     <li>{@code azure.sdk.sharedpool.size} - The size of the shared executor service. If not set, it defaults to 10
 *     times the number of available processors.</li>
 *     <li>{@code azure.sdk.sharedpool.keepalivemillis} - The keep alive time for threads in the shared executor
 *     service. If not set, it defaults to 60 seconds.</li>
 *     <li>{@code azure.sdk.sharedpool.virtual} - A boolean flag to indicate if the shared executor service should use
 *     virtual threads. If not set, it defaults to true. Ignored if virtual threads are not available in the
 *     runtime.</li>
 * </ul>
 *
 * If a custom executor service is set using {@link #setExecutorService(ExecutorService)}, it updates all references to
 * the shared executor service. Meaning, if another area in code already had a reference to the shared instance, it will
 * now use the passed executor service to execute tasks.
 * <p>
 * Calls to {@link #shutdown()} and {@link #shutdownNow()} are not supported. If the default shared executor service is
 * being used, it is bound to the lifecycle of the application and will be shutdown when the application is shutdown.
 * If a custom executor service is set using {@link #setExecutorService(ExecutorService)}, it is the responsibility of
 * the caller to manage the lifecycle of the executor service.
 * <p>
 * If a custom executor service is set using {@link #setExecutorService(ExecutorService)}, and it is shutdown or
 * terminated, the shared executor service will be reset to the default shared executor service.
 */
@SuppressWarnings({ "resource", "NullableProblems" })
public final class SharedExecutorService implements ExecutorService {
    private static final ClientLogger LOGGER = new ClientLogger(SharedExecutorService.class);

    private static final SharedExecutorService INSTANCE = new SharedExecutorService();

    // Shared thread counter for all instances of SharedExecutorService created using the empty factory method.
    private static final AtomicLong AZURE_SDK_THREAD_COUNTER = new AtomicLong();
    private static final String AZURE_SDK_THREAD_NAME = "azure-sdk-global-thread-";

    // The thread pool size for the shared executor service.
    //
    // This uses the configuration setting 'azure.sdk.sharedpool.size' if set, otherwise it defaults to 10 times the
    // number of available processors.
    // If 'azure.sdk.sharedpool.size' is set to a non-integer, negative value, or zero, the default value is used.
    private static final int THREAD_POOL_SIZE
        = getConfig("azure.sdk.sharedpool.size", Integer::parseInt, 10 * Runtime.getRuntime().availableProcessors());

    // The thread pool keep alive time for the shared executor service.
    //
    // This uses the configuration setting 'azure.sdk.sharedpool.keepalivemillis' if set, otherwise it defaults to 60
    // seconds.
    // If 'azure.sdk.sharedpool.keepalivemillis' is set to a non-integer, negative value, or zero, the default value is
    // used.
    private static final long THREAD_POOL_KEEP_ALIVE_MILLIS
        = getConfig("azure.sdk.sharedpool.keepalivemillis", Long::parseLong, 60_000L);

    // Virtual thread support for the shared executor service.
    //
    // This uses the configuration setting 'azure.sdk.sharedpool.virtual' if set, otherwise it defaults to true.
    private static final boolean THREAD_POOL_VIRTUAL
        = getConfig("azure.sdk.sharedpool.virtual", Boolean::parseBoolean, true);

    private static <T> T getConfig(String configurationName, Function<String, T> parser, T defaultValue) {
        String value = Configuration.getGlobalConfiguration().get(configurationName);
        if (value == null) {
            LOGGER.verbose("Configuration '{}' is not set, using default value '{}'.", configurationName, defaultValue);
            return defaultValue;
        }

        try {
            T returnValue = parser.apply(value);
            LOGGER.verbose("Configuration '{}' is set to '{}'.", configurationName, returnValue);
            return parser.apply(value);
        } catch (NumberFormatException e) {
            LOGGER.info("Configuration '{}' is set to an invalid value '{}', using default value '{}'.",
                configurationName, value, defaultValue);
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
            LOGGER.verbose("Virtual threads are supported in the current runtime.");
        } catch (Exception | LinkageError e) {
            LOGGER.verbose("Virtual threads are not supported in the current runtime.", e);
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

    private final AtomicReference<ExecutorWithMetadata> wrappedExecutorService;

    private SharedExecutorService() {
        this.wrappedExecutorService = new AtomicReference<>();
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
     *
     * @param executorService The executor service to set as the shared instance.
     * @throws NullPointerException If the passed executor service is null.
     * @throws IllegalStateException If the passed executor service is shutdown or terminated.
     */
    public static void setExecutorService(ExecutorService executorService) {
        // We allow for the global executor service to be set from an external source to allow for consumers of the SDK
        // to use their own thread management to run Azure SDK tasks. This allows for the SDKs to perform deeper
        // integration into an environment, such as the consumer environment knowing details about capacity, allowing
        // the custom executor service to better manage resources than our more general 10x the number of processors.
        // Another scenario could be an executor service that creates threads with specific permissions, such as
        // allowing Azure Core or Jackson to perform deep reflection on classes that are not normally allowed.
        Objects.requireNonNull(executorService, "'executorService' cannot be null.");
        if (executorService.isShutdown() || executorService.isTerminated()) {
            throw new IllegalStateException("The passed executor service is shutdown or terminated.");
        }

        ExecutorWithMetadata existing
            = INSTANCE.wrappedExecutorService.getAndSet(new ExecutorWithMetadata(executorService, null));

        if (existing != null) {
            // This is calling ExecutorWithMetadata.shutdown() which will shutdown the executor service if it was
            // created by this class. Otherwise, it's a no-op.
            existing.shutdown();
        }
    }

    /**
     * Shutdown isn't supported for this executor service as it is shared by multiple consumers.
     * <p>
     * Calling this method will result in an {@link UnsupportedOperationException} being thrown.
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
        return wrappedExecutorService.updateAndGet(wrapper -> {
            if (wrapper == null || wrapper.executorService.isShutdown() || wrapper.executorService.isTerminated()) {
                return createSharedExecutor();
            } else {
                return wrapper;
            }
        }).executorService;
    }

    private static ExecutorWithMetadata createSharedExecutor() {
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

        ExecutorService executorService = new ThreadPoolExecutor(0, THREAD_POOL_SIZE, THREAD_POOL_KEEP_ALIVE_MILLIS,
            TimeUnit.MILLISECONDS, new SynchronousQueue<>(), threadFactory);
        Thread shutdownThread = CoreUtils.createExecutorServiceShutdownThread(executorService, Duration.ofSeconds(5));
        CoreUtils.addShutdownHookSafely(shutdownThread);

        return new ExecutorWithMetadata(executorService, shutdownThread);
    }

    private static ThreadFactory createVirtualThreadFactory() throws Exception {
        Object virtualThreadBuilder = GET_VIRTUAL_THREAD_BUILDER.invokeStatic();
        SET_VIRTUAL_THREAD_BUILDER_THREAD_NAME.invokeWithArguments(virtualThreadBuilder, AZURE_SDK_THREAD_NAME);
        return (ThreadFactory) CREATE_VIRTUAL_THREAD_FACTORY.invokeWithArguments(virtualThreadBuilder);
    }

    private static ThreadFactory createNonVirtualThreadFactory() {
        return r -> {
            Thread thread = new Thread(r, AZURE_SDK_THREAD_NAME + AZURE_SDK_THREAD_COUNTER.getAndIncrement());
            thread.setDaemon(true);

            return thread;
        };
    }

    private static final class ExecutorWithMetadata {
        private final Thread shutdownThread;
        private final ExecutorService executorService;

        ExecutorWithMetadata(ExecutorService executorService, Thread shutdownThread) {
            this.executorService = executorService;
            this.shutdownThread = shutdownThread;
        }

        void shutdown() {
            // The executor service is only shutdown if there is a shutdown thread as that indicates the executor
            // service was created by this class.
            if (shutdownThread != null) {
                executorService.shutdown();
                CoreUtils.removeShutdownHookSafely(shutdownThread);
            }
        }
    }
}
