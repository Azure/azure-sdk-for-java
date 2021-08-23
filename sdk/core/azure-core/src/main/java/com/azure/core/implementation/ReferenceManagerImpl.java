// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.util.ReferenceManager;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The implementation that {@link ReferenceManager} uses.
 */
public final class ReferenceManagerImpl implements Runnable {
    // Used to create a unique thread which is used to clean up the resources managed by an instance ReferenceManager.
    private static final AtomicInteger RESOURCE_MANAGER_THREAD_NUMBER = new AtomicInteger();

    // Base name for ResourceManager threads.
    private static final String BASE_THREAD_NAME = "azure-sdk-referencemanager-";

    private final CleanableReference<?> cleanableReferenceList;
    private final ReferenceQueue<Object> queue;

    /**
     * Creates a new instance of {@link ReferenceManagerImpl}.
     */
    public ReferenceManagerImpl() {
        this.queue = new ReferenceQueue<>();
        this.cleanableReferenceList = new CleanableReference<>();

        new CleanableReference<>(this, () -> { }, this);

        Thread thread = new Thread(Thread.currentThread().getThreadGroup(), this,
            BASE_THREAD_NAME + RESOURCE_MANAGER_THREAD_NUMBER.getAndIncrement());
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Registers the {@code object} anc the cleaning action to run once the object becomes phantom reachable.
     * <p>
     * The {@code cleanupAction} cannot have a reference to the {@code object}, otherwise the object will never be able
     * to become phantom reachable.
     * <p>
     * Exceptions thrown by {@code cleanupAction} are ignored.
     *
     * @param object The object to monitor.
     * @param cleanupAction The cleanup action to perform when the {@code object} becomes phantom reachable.
     */
    public void register(Object object, Runnable cleanupAction) {
        new CleanableReference<>(object, cleanupAction, this);
    }

    @Override
    public void run() {
        while (cleanableReferenceList.hasRemaining()) {
            try {
                CleanableReference<?> reference = (CleanableReference<?>) queue.remove(30000);
                if (reference != null) {
                    reference.clean();
                }
            } catch (Throwable e) {
                // Ignore the exception.
            }
        }
    }

    /*
     * This class manages maintaining a reference to an object that will trigger a cleanup action once it is phantom
     * reachable.
     */
    private static final class CleanableReference<T> extends PhantomReference<T> {
        // The cleanup action to run once the reference is phantom reachable.
        private final Runnable cleanupAction;

        // The list of cleanable references.
        private final CleanableReference<?> cleanupList;

        CleanableReference<?> previous = this;
        CleanableReference<?> next = this;

        CleanableReference() {
            super(null, null);
            this.cleanupAction = null;
            this.cleanupList = this;
        }

        CleanableReference(T referent, Runnable cleanupAction, ReferenceManagerImpl manager) {
            super(Objects.requireNonNull(referent, "'referent' cannot be null."), manager.queue);
            this.cleanupAction = cleanupAction;
            this.cleanupList = manager.cleanableReferenceList;
            insert();
        }

        public void clean() {
            if (remove()) {
                super.clear();
                cleanupAction.run();
            }
        }

        @Override
        public void clear() {
            if (remove()) {
                super.clear();
            }
        }

        boolean hasRemaining() {
            synchronized (cleanupList) {
                return cleanupList != cleanupList.next;
            }
        }

        private void insert() {
            synchronized (cleanupList) {
                previous = cleanupList;
                next = cleanupList.next;
                next.previous = this;
                cleanupList.next = this;
            }
        }

        private boolean remove() {
            synchronized (cleanupList) {
                if (next != this) {
                    next.previous = previous;
                    previous.next = next;
                    previous = this;
                    next = this;
                    return true;
                }

                return false;
            }
        }
    }
}
