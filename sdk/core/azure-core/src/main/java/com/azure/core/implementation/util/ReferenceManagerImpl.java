// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.ReferenceManager;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;

import java.lang.ref.ReferenceQueue;
import java.util.Objects;

// This is the base implementation of ReferenceManager, there is another Java 9 specific implementation in
// /src/main/java9 for multi-release JARs.
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

    private final CleanableReference<?> cleanableReferenceList;
    private final ReferenceQueue<Object> queue;

    /**
     * Creates a new instance of {@link ReferenceManagerImpl}.
     */
    public ReferenceManagerImpl() {
        this.queue = new ReferenceQueue<>();
        this.cleanableReferenceList = new CleanableReference<>();

        Thread thread = new Thread(this::clearReferenceQueue, BASE_THREAD_NAME);

        // Register this instance of ReferenceManager as the head of the cleaning queue. Doing so will allow the
        // ReferenceManager to clean itself up when no longer in use, for now it simply shuts down the backing thread.
        new CleanableReference<>(this, () -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                LOGGER.log(LogLevel.WARNING, () -> "Failed to shutdown ReferenceManager thread.", e);
            }
        }, this);

        // If multiple instances of ReferenceManager needs to be supported each Thread should have a unique name with a
        // consistent base name.
        // Thread thread = new Thread(Thread.currentThread().getThreadGroup(), this,
        //     BASE_THREAD_NAME + "-" + RESOURCE_MANAGER_THREAD_NUMBER.getAndIncrement());

        // Make the ReferenceManager Thread a daemon, this will prevent it from halting a JVM shutdown.
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void register(Object object, Runnable cleanupAction) {
        new CleanableReference<>(Objects.requireNonNull(object, "'object' cannot be null."),
            Objects.requireNonNull(cleanupAction, "'cleanupAction' cannot be null."), this);
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

    static int getJavaImplementationMajorVersion() {
        return 8;
    }

    ReferenceQueue<Object> getQueue() {
        return this.queue;
    }

    CleanableReference<?> getCleanableReferenceList() {
        return cleanableReferenceList;
    }
}
