// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.implementation.ReflectionUtils;
import com.azure.core.implementation.ReflectiveInvoker;

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

/**
 * An {@link ExecutorService} that is shared by multiple consumers.
 * <p>
 * On creation of this executor service, it will register a runtime shutdown hook and reference shutdown hook to ensure
 * it is cleaned up when either the JVM exits or is no longer being referenced.
 * <p>
 * This executor service may be created by using its constructor, but it is recommended to use the shared instance.
 */
public final class SharedExecutorService implements ExecutorService {
    private static final AtomicReference<SharedExecutorService> INSTANCE = new AtomicReference<>();

    // Shared thread counter for all instances of SharedExecutorService created using the empty factory method.
    private static final AtomicLong AZURE_SDK_THREAD_COUNTER = new AtomicLong();
    private static final String AZURE_SDK_THREAD_NAME = "azure-sdk-global-thread-";

    // The thread pool size for the shared executor service.
    //
    // This uses the configuration setting 'azure.sdk.threadPoolSize' if set, otherwise it defaults to 10 times the
    // number of available processors.
    // If 'azure.sdk.threadPoolSize' is set to a non-integer, negative value, or zero, the default value is used.
    private static final int THREAD_POOL_SIZE
        = Configuration.getGlobalConfiguration().get("azure.sdk.threadPoolSize", config -> {
            try {
                int size = Integer.parseInt(config);
                if (size <= 0) {
                    return 10 * Runtime.getRuntime().availableProcessors();
                } else {
                    return size;
                }
            } catch (NumberFormatException ignored) {
                return 10 * Runtime.getRuntime().availableProcessors();
            }
        });

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
        } catch (Exception | LinkageError e) {
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

    private final ExecutorService wrappedExecutorService;
    private final boolean internal;

    private SharedExecutorService(ExecutorService executorService, boolean internal) {
        this.wrappedExecutorService = executorService;
        this.internal = internal;
    }

    /**
     * Gets the shared instance of the executor service.
     *
     * @return The shared instance of the executor service.
     */
    public static SharedExecutorService getInstance() {
        return INSTANCE.updateAndGet(instance -> {
            if (instance == null) {
                return new SharedExecutorService(createSharedExecutor(), true);
            } else {
                return instance;
            }
        });
    }

    /**
     * Sets the shared instance using the passed executor service.
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
    public static void setInstance(ExecutorService executorService) {
        Objects.requireNonNull(executorService, "'executorService' cannot be null.");
        if (executorService.isShutdown() || executorService.isTerminated()) {
            throw new IllegalStateException("The passed executor service is shutdown or terminated.");
        }

        SharedExecutorService existing = INSTANCE.getAndSet(new SharedExecutorService(executorService, false));
        if (existing != null && existing.internal) {
            existing.wrappedExecutorService.shutdown();
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
        throw new UnsupportedOperationException("This executor service is shared and cannot be shut down.");
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
        throw new UnsupportedOperationException("This executor service is shared and cannot be shut down.");
    }

    @Override
    public boolean isShutdown() {
        return wrappedExecutorService.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return wrappedExecutorService.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
        return false;
    }

    @Override
    public void execute(Runnable command) {
        wrappedExecutorService.execute(command);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return wrappedExecutorService.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return wrappedExecutorService.submit(task, result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return wrappedExecutorService.submit(task);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return wrappedExecutorService.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
        throws InterruptedException {
        return wrappedExecutorService.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return wrappedExecutorService.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        return wrappedExecutorService.invokeAny(tasks, timeout, unit);
    }

    private static ExecutorService createSharedExecutor() {
        ThreadFactory threadFactory;
        if (VIRTUAL_THREAD_SUPPORTED) {
            try {
                threadFactory = createVirtualThreadFactory();
            } catch (Exception e) {
                threadFactory = createNonVirtualThreadFactory();
            }
        } else {
            threadFactory = createNonVirtualThreadFactory();
        }

        ExecutorService sharedExecutor = CoreUtils.addShutdownHookSafely(
            new ThreadPoolExecutor(0, THREAD_POOL_SIZE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), threadFactory),
            Duration.ofSeconds(5));

        // Register the shared executor with the ReferenceManager to ensure it shuts down if it becomes unreferenced.
        ReferenceManager.INSTANCE.register(sharedExecutor, sharedExecutor::shutdown);

        return sharedExecutor;
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
}
