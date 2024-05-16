// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.eventprocessor;

/**
 * Abstract class for event awaiters.
 */
public abstract class EventAwaiter implements AutoCloseable {
    private boolean disposed = false;

    @Override
    public void close() {
        disposed = true;
    }

    public boolean isDisposed() {
        return disposed;
    }

    public abstract void onEventsReceived(EventWithBacklogId event);
}
