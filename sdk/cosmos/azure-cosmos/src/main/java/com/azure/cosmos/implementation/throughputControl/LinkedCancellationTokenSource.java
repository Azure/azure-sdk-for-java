// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl;

import java.io.Closeable;

/**
 * Used in the throughput controller hierarchy.
 *
 * There are many cases a higher level controller need to close the lower level controller and create a new one.
 * A {@link LinkedCancellationToken} will be passed down to lower level controller so to cancel all the underlying tasks.
 */
public class LinkedCancellationTokenSource implements Closeable {
    private boolean tokenSourceClosed;
    private LinkedCancellationToken parentToken;

    public LinkedCancellationTokenSource() {
        this(null);
    }

    public LinkedCancellationTokenSource(LinkedCancellationToken parent) {
        this.tokenSourceClosed = false;
        if (parent != null) {
            parent.register(this);
            this.parentToken = parent;
        }
    }

    public LinkedCancellationToken getToken() {
        synchronized (this) {
            if (this.tokenSourceClosed) {
                throw new IllegalStateException("The cancellation token resource has been closed");
            }

            return new LinkedCancellationToken(this);
        }
    }

    public boolean isClosed() {
        synchronized (this) {
            return this.tokenSourceClosed || (this.parentToken != null && this.parentToken.isCancellationRequested());
        }
    }
    @Override
    public void close() {
        synchronized (this) {
            this.tokenSourceClosed = true;
        }
    }
}
