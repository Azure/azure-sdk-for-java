// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Used in the throughput controller hierarchy.
 *
 * There are many cases a higher level controller need to close the lower level controller and create a new one.
 * A {@link LinkedCancellationToken} will be passed down to lower level controller so to cancel all the underlying tasks.
 */
public class LinkedCancellationTokenSource implements Closeable {
    private AtomicBoolean tokenSourceClosed;
    private LinkedCancellationToken parentToken;

    public LinkedCancellationTokenSource() {
        this(null);
    }

    public LinkedCancellationTokenSource(LinkedCancellationToken parent) {
        this.tokenSourceClosed = new AtomicBoolean(false);
        if (parent != null) {
            parent.register(this);
            this.parentToken = parent;
        }
    }

    public LinkedCancellationToken getToken() {
        return new LinkedCancellationToken(this);
    }

    public boolean isClosed() {
        return this.tokenSourceClosed.get() || (this.parentToken != null && this.parentToken.isCancellationRequested());
    }
    @Override
    public void close() {
        this.tokenSourceClosed.set(true);
    }
}
