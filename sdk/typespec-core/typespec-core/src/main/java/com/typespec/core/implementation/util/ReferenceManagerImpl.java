// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.util;

import com.typespec.core.util.ReferenceManager;
import com.typespec.core.util.logging.ClientLogger;
import com.typespec.core.util.logging.LogLevel;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.ref.ReferenceQueue;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;

/**
 * Implementation of {@link ReferenceManager}.
 */
public final class ReferenceManagerImpl implements ReferenceManager {
    private static final ClientLogger LOGGER = new ClientLogger(ReferenceManagerImpl.class);

    // If multiple instances of ReferenceManaged needs to be supported this maintains Thread name uniqueness.
    // Used to create a unique thread which is used to clean up the resources managed by an instance ReferenceManager.
    // private static final AtomicInteger RESOURCE_MANAGER_THREAD_NUMBER = new AtomicInteger();

    // Base name for ResourceManager threads.
    private static final String BASE_THREAD_NAME = "azure-sdk-referencemanager";

    private static final Object CLEANER;
    private static final MethodHandle CLEANER_REGISTER;

    static {
        Object cleaner = null;
        MethodHandle cleanerRegister = null;
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> cleanerClass = Class.forName("java.lang.ref.Cleaner");
            cleaner = cleanerClass.getDeclaredMethod("create", ThreadFactory.class)
                .invoke(null, (ThreadFactory) r -> new Thread(r, BASE_THREAD_NAME));
            cleanerRegister = lookup.unreflect(cleanerClass.getMethod("register", Object.class, Runnable.class));
        } catch (ReflectiveOperationException ex) {
            LOGGER.log(LogLevel.VERBOSE, () -> "Unable to use java.lang.ref.Cleaner to manage references.", ex);
        }

        CLEANER = cleaner;
        CLEANER_REGISTER = cleanerRegister;
    }

    private final CleanableReference<?> cleanableReferenceList;
    private final ReferenceQueue<Object> queue;

    /**
     * Creates a new instance of {@link ReferenceManagerImpl}.
     */
    public ReferenceManagerImpl() {
        if (CLEANER == null) {
            this.queue = new ReferenceQueue<>();
            this.cleanableReferenceList = new CleanableReference<>();

            Thread thread = new Thread(this::clearReferenceQueue, BASE_THREAD_NAME);

            // Register this instance of ReferenceManager as the head of the cleaning queue. Doing so will allow the
            // ReferenceManager to clean itself up when no longer in use, for now it simply shuts down the backing
            // thread.
            new CleanableReference<>(this, () -> {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    LOGGER.log(LogLevel.WARNING, () -> "Failed to shutdown ReferenceManager thread.", e);
                }
            }, this);

            // If multiple instances of ReferenceManager needs to be supported each Thread should have a unique name
            // with a consistent base name.
            // Thread thread = new Thread(Thread.currentThread().getThreadGroup(), this,
            //     BASE_THREAD_NAME + "-" + RESOURCE_MANAGER_THREAD_NUMBER.getAndIncrement());

            // Make the ReferenceManager Thread a daemon, this will prevent it from halting a JVM shutdown.
            thread.setDaemon(true);
            thread.start();
        } else {
            this.queue = null;
            this.cleanableReferenceList = null;
        }
    }

    @Override
    public void register(Object object, Runnable cleanupAction) {
        Objects.requireNonNull(object, "'object' cannot be null.");
        Objects.requireNonNull(cleanupAction, "'cleanupAction' cannot be null.");

        if (CLEANER == null) {
            new CleanableReference<>(object, cleanupAction, this);
        } else {
            try {
                CLEANER_REGISTER.invoke(CLEANER, object, cleanupAction);
            } catch (Throwable throwable) {
                if (throwable instanceof Error) {
                    throw (Error) throwable;
                } else if (throwable instanceof RuntimeException) {
                    throw LOGGER.logExceptionAsError((RuntimeException) throwable);
                } else {
                    throw LOGGER.logExceptionAsError(new RuntimeException(throwable));
                }
            }
        }
    }

    /*
     * Attempts to clear the reference queue managed by the ReferenceManager.
     *
     * This will run in a loop until either the application is shutdown or the ReferenceManager itself is no longer
     * being referenced
     */
    void clearReferenceQueue() {
        while (cleanableReferenceList.hasRemaining()) {
            CleanableReference<?> reference = null;
            try {
                // Block the ReferenceManager thread until either an element is emitted from the queue or 30 seconds
                // elapses.
                reference = (CleanableReference<?>) queue.remove(30000);
            } catch (InterruptedException ex) {
                // Thread has been interrupted while waiting for the queue to emit an element.
                LOGGER.log(LogLevel.VERBOSE,
                    () -> "ReferenceManager Thread interrupted while waiting for a reference to clean.", ex);
            }

            try {
                if (reference != null) {
                    reference.clean();
                }
            } catch (Exception ex) {
                // Cleaning action threw an exception.
                LOGGER.log(LogLevel.INFORMATIONAL, () -> "Cleaning a reference threw an exception.", ex);
            }
        }
    }

    static boolean isCleanerUsed() {
        return CLEANER != null;
    }

    ReferenceQueue<Object> getQueue() {
        return this.queue;
    }

    CleanableReference<?> getCleanableReferenceList() {
        return cleanableReferenceList;
    }
}
