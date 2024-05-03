// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.okhttp.implementation;

import okhttp3.Call;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Implementation of {@link Call} which applies a per-call response timeout to the call.
 */
public final class PerCallTimeoutCall {
    // Singleton timer to schedule timeout tasks.
    // TODO (alzimmer): Make sure one thread is sufficient for all timeout tasks.
    private static final Timer TIMER = new Timer("azure-okhttp-response-timeout-tracker", true);

    private final long perCallTimeout;

    private volatile boolean timedOut;

    private static final AtomicReferenceFieldUpdater<PerCallTimeoutCall, TimerTask> CURRENT_TIMEOUT_UPDATER
        = AtomicReferenceFieldUpdater.newUpdater(PerCallTimeoutCall.class, TimerTask.class, "currentTimeout");
    private volatile TimerTask currentTimeout;

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
            TimerTask currentTimeout = new PerCallTimerTask(this, call);

            TIMER.schedule(currentTimeout, perCallTimeout);
            TimerTask existing = CURRENT_TIMEOUT_UPDATER.getAndSet(this, currentTimeout);
            if (existing != null) {
                existing.cancel();
            }
        }
    }

    /**
     * Cancels the per-call timeout task.
     * <p>
     * Cancellations happen if the response returned before the timeout or if the call was cancelled externally.
     */
    public void endPerCallTimeout() {
        TimerTask currentTimeout = CURRENT_TIMEOUT_UPDATER.getAndSet(this, null);
        if (currentTimeout != null) {
            currentTimeout.cancel();
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

    private static final class PerCallTimerTask extends TimerTask {
        private final PerCallTimeoutCall perCallTimeoutCall;
        private final Call call;

        PerCallTimerTask(PerCallTimeoutCall perCallTimeoutCall, Call call) {
            this.perCallTimeoutCall = perCallTimeoutCall;
            this.call = call;
        }

        @Override
        public void run() {
            // Set timeout first.
            perCallTimeoutCall.timedOut = true;
            call.cancel();
        }
    }
}
