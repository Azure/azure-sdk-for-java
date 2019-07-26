// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

final class ActiveClientTokenManager {

    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(ActiveClientTokenManager.class);
    private final Object timerLock;
    private final Runnable sendTokenTask;
    private final ClientEntity clientEntity;
    private final Duration tokenRefreshInterval;
    private final SchedulerProvider schedulerProvider;
    private final Timer timerScheduler;
    private CompletableFuture<?> timer;

    ActiveClientTokenManager(
            final ClientEntity clientEntity,
            final Runnable sendTokenAsync,
            final Duration tokenRefreshInterval,
            final SchedulerProvider schedulerProvider) {

        this.sendTokenTask = sendTokenAsync;
        this.clientEntity = clientEntity;
        this.tokenRefreshInterval = tokenRefreshInterval;
        this.timerLock = new Object();
        this.schedulerProvider = schedulerProvider;
        this.timerScheduler = new Timer(schedulerProvider);

        synchronized (this.timerLock) {
            this.timer = this.timerScheduler.schedule(new TimerCallback(), tokenRefreshInterval);
        }
    }

    public void cancel() {
        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format(Locale.US, "clientEntity[%s] - canceling ActiveClientLinkManager",
                    clientEntity.getClientId()));
        }

        synchronized (this.timerLock) {
            this.timer.cancel(false);
        }
    }

    private class TimerCallback implements Runnable {

        @Override
        public void run() {

            if (!clientEntity.getIsClosingOrClosed()) {

                sendTokenTask.run();

                synchronized (ActiveClientTokenManager.this.timerLock) {
                    ActiveClientTokenManager.this.timer = ActiveClientTokenManager.this.timerScheduler.schedule(new TimerCallback(), tokenRefreshInterval);
                }
            } else {

                if (TRACE_LOGGER.isInfoEnabled()) {
                    TRACE_LOGGER.info(String.format(Locale.US, "clientEntity[%s] - closing ActiveClientLinkManager",
                            clientEntity.getClientId()));
                }
            }
        }

    }
}
