// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.eventprocessor;

/**
 * Abstract class for event awaiters.
 */
public abstract class EventAwaiter implements AutoCloseable {
    private boolean disposed = false;

    @Override
    public void close() {
        disposed = true;
    }

    boolean isDisposed() {
        return disposed;
    }

    abstract void onEventsReceived(EventWithBacklogId event);
}
