// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class WorkItem<T> {
    private final TimeoutTracker tracker;
    private final CompletableFuture<T> work;

    public WorkItem(final CompletableFuture<T> completableFuture, final Duration timeout) {
        this(completableFuture, TimeoutTracker.create(timeout));
    }

    public WorkItem(final CompletableFuture<T> completableFuture, final TimeoutTracker tracker) {
        this.work = completableFuture;
        this.tracker = tracker;
    }

    public TimeoutTracker getTimeoutTracker() {
        return this.tracker;
    }

    public CompletableFuture<T> getWork() {
        return this.work;
    }
}
