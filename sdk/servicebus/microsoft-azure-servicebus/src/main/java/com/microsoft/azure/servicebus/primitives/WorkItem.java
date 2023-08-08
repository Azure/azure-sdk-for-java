// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.primitives;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;

class WorkItem<T> {
    private final TimeoutTracker tracker;
    private final CompletableFuture<T> work;
    private ScheduledFuture<?> timeoutTask;
    private Exception lastKnownException;

    WorkItem(final CompletableFuture<T> completableFuture, final Duration timeout) {
        this(completableFuture, TimeoutTracker.create(timeout));
    }

    WorkItem(final CompletableFuture<T> completableFuture, final TimeoutTracker tracker) {
        this.work = completableFuture;
        this.tracker = tracker;
    }

    public TimeoutTracker getTimeoutTracker() {
        return this.tracker;
    }

    // TODO; remove this method. Synchronize calls to complete on the future so two different threads don't attempt to complete at the same time.
    // Also group complete and canceling timeout task in one method so calling code doesn't have to call both of them one after the other.
    public CompletableFuture<T> getWork() {
        return this.work;
    }

    public ScheduledFuture<?> getTimeoutTask() {
        return this.timeoutTask;
    }

    public void setTimeoutTask(final ScheduledFuture<?> timeoutTask) {
        this.timeoutTask = timeoutTask;
    }

    public boolean cancelTimeoutTask(boolean mayInterruptIfRunning) {
        if (this.timeoutTask != null) {
            return this.timeoutTask.cancel(mayInterruptIfRunning);
        }

        return false;
    }

    public Exception getLastKnownException() {
        return this.lastKnownException;
    }

    public void setLastKnownException(Exception exception) {
        this.lastKnownException = exception;
    }
}
