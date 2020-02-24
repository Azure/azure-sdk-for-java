// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

import java.io.Closeable;
import java.io.IOException;

/**
 * Signals to a {@link CancellationToken} that it should be canceled..
 */
public class CancellationTokenSource implements Closeable {

    private volatile boolean tokenSourceClosed;
    private volatile boolean cancellationRequested;

    public CancellationTokenSource() {
        this.tokenSourceClosed = false;
        this.cancellationRequested = false;
    }

    public synchronized boolean isCancellationRequested() {
        if (tokenSourceClosed) {
            throw new IllegalStateException("Object already closed");
        }

        return this.cancellationRequested;
    }

    public CancellationToken getToken() {
        return new CancellationToken(this);
    }

    public synchronized void cancel() {
        this.cancellationRequested = true;
    }

    @Override
    public synchronized void close() throws IOException {
        if (tokenSourceClosed) return;
    }
}
