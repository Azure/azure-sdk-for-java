// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed;

/**
 * Propagates notification that operations should be canceled..
 */
public class CancellationToken {
    private final CancellationTokenSource tokenSource;
    private volatile boolean cancellationRequested;

    public CancellationToken(CancellationTokenSource source) {
        this.tokenSource = source;
        cancellationRequested = false;
    }

    public synchronized void cancel() {
        this.cancellationRequested = true;
    }

    /**
     * @return true if the cancellation was requested from the source.
     */
    public boolean isCancellationRequested() {
        return tokenSource.isCancellationRequested() || this.cancellationRequested;
    }
}
