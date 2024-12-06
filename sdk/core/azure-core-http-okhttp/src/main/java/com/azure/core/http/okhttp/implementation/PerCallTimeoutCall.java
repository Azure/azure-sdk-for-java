// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.okhttp.implementation;

import com.azure.core.util.SharedExecutorService;
import okhttp3.Call;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Implementation of {@link Call} which applies a per-call response timeout to the call.
 */
@SuppressWarnings("rawtypes")
public final class PerCallTimeoutCall {
    private final long perCallTimeout;

    private volatile boolean timedOut;

    private static final AtomicReferenceFieldUpdater<PerCallTimeoutCall, ScheduledFuture> CURRENT_TIMEOUT_UPDATER
        = AtomicReferenceFieldUpdater.newUpdater(PerCallTimeoutCall.class, ScheduledFuture.class, "currentTimeout");
    private volatile ScheduledFuture currentTimeout;

    /**
     * Creates a new instance of PerCallTimeoutCall.
     *
     * @param perCallTimeout The per-call timeout to apply to the call.
     */
    public PerCallTimeoutCall(long perCallTimeout) {
        this.perCallTimeout = perCallTimeout;
    }

    /**
     * Starts the timer task to time out the call if it exceeds the per-call timeout.
     * <p>
     * No timeout is applied if the per-call timeout is less than or equal to 0.
     *
     * @param call The call to apply the timeout to.
     */
    public void beginPerCallTimeout(Call call) {
        if (perCallTimeout > 0) {
            ScheduledFuture<?> future = SharedExecutorService.getInstance().schedule(() -> {
                this.timedOut = true;
                call.cancel();
            }, perCallTimeout, TimeUnit.MILLISECONDS);
            ScheduledFuture<?> existing = CURRENT_TIMEOUT_UPDATER.getAndSet(this, future);
            if (existing != null) {
                existing.cancel(false);
            }
        }
    }

    /**
     * Cancels the per-call timeout task.
     * <p>
     * Cancellations happen if the response returned before the timeout or if the call was cancelled externally.
     */
    public void endPerCallTimeout() {
        ScheduledFuture<?> currentTimeout = CURRENT_TIMEOUT_UPDATER.getAndSet(this, null);
        if (currentTimeout != null) {
            currentTimeout.cancel(false);
        }
    }

    /**
     * Returns whether the call timed out.
     *
     * @return Whether the call timed out.
     */
    public boolean isTimedOut() {
        return timedOut;
    }
}
