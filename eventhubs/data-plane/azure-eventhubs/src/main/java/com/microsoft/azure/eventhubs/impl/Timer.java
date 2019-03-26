// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;

final class Timer {

    final SchedulerProvider schedulerProvider;

    Timer(final SchedulerProvider schedulerProvider) {
        this.schedulerProvider = schedulerProvider;
    }

    public CompletableFuture<?> schedule(
            final Runnable runnable,
            final Duration runAfter) {

        final ScheduledTask scheduledTask = new ScheduledTask(runnable);
        final CompletableFuture<?> taskHandle = scheduledTask.getScheduledFuture();
        try {
            this.schedulerProvider.getReactorDispatcher().invoke((int) runAfter.toMillis(), scheduledTask);
        } catch (IOException | RejectedExecutionException e) {
            taskHandle.completeExceptionally(e);
        }

        return taskHandle;
    }

    static final class ScheduledTask extends DispatchHandler {

        final CompletableFuture<?> scheduledFuture;
        final Runnable runnable;

        ScheduledTask(final Runnable runnable) {
            this.runnable = runnable;
            this.scheduledFuture = new CompletableFuture<>();
        }

        @Override
        public void onEvent() {
            if (!scheduledFuture.isCancelled()) {
                try {
                    runnable.run();
                    scheduledFuture.complete(null);
                } catch (Exception exception) {
                    scheduledFuture.completeExceptionally(exception);
                }
            }
        }

        public CompletableFuture<?> getScheduledFuture() {
            return this.scheduledFuture;
        }
    }
}
