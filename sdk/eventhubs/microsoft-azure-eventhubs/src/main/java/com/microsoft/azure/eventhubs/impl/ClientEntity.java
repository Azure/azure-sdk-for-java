// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import com.microsoft.azure.eventhubs.EventHubException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Contract for all client entities with Open-Close/Abort state m/c
 * main-purpose: closeAll related entities
 * Internal-class
 */
abstract class ClientEntity {

    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(ClientEntity.class);
    protected final ScheduledExecutorService executor;
    private final String clientId;
    private final Object syncClose;
    private final ClientEntity parent;
    private CompletableFuture<Void> closeTask;
    private boolean isClosing;
    private boolean isClosed;

    protected ClientEntity(final String clientId, final ClientEntity parent, final ScheduledExecutorService executor) {
        this.clientId = clientId;
        this.parent = parent;
        this.executor = executor;

        this.syncClose = new Object();
    }

    protected abstract CompletableFuture<Void> onClose();

    public String getClientId() {
        return this.clientId;
    }

    boolean getIsClosed() {
        final boolean isParentClosed = this.parent != null && this.parent.getIsClosed();
        synchronized (this.syncClose) {
            return isParentClosed || this.isClosed;
        }
    }

    // returns true even if the Parent is (being) Closed
    boolean getIsClosingOrClosed() {
        final boolean isParentClosingOrClosed = this.parent != null && this.parent.getIsClosingOrClosed();
        synchronized (this.syncClose) {
            return isParentClosingOrClosed || this.isClosing || this.isClosed;
        }
    }

    // used to force close when entity is faulted
    protected final void setClosed() {
        synchronized (this.syncClose) {
            this.isClosed = true;
        }
    }

    public final CompletableFuture<Void> close() {
        synchronized (this.syncClose) {
            if (this.isClosed || this.isClosing) {
                return this.closeTask == null ? CompletableFuture.completedFuture(null) : this.closeTask;
            }
            this.isClosing = true;
        }

        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info("close: clientId[" + this.clientId + "]");
        }

        this.closeTask = this.onClose().thenRunAsync(new Runnable() {
            @Override
            public void run() {
                synchronized (ClientEntity.this.syncClose) {
                    ClientEntity.this.isClosing = false;
                    ClientEntity.this.isClosed = true;
                }
            }
        }, this.executor);

        return this.closeTask;
    }

    public final void closeSync() throws EventHubException {
        try {
            this.close().get();
        } catch (InterruptedException | ExecutionException exception) {
            if (exception instanceof InterruptedException) {
                // Re-assert the thread's interrupted status
                Thread.currentThread().interrupt();
            }

            final Throwable throwable = exception.getCause();
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            } else if (throwable instanceof EventHubException) {
                throw (EventHubException) throwable;
            } else {
                throw new RuntimeException(throwable != null ? throwable : exception);
            }
        }
    }

    protected final void throwIfClosed() {
        if (this.getIsClosingOrClosed()) {
            throw new IllegalStateException(String.format(Locale.US, "Operation not allowed after the %s instance is Closed.", this.getClass().getName()), this.getLastKnownError());
        }
    }

    protected Exception getLastKnownError() {
        return null;
    }
}
