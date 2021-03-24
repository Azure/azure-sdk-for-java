// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Propagates notification that operations should be canceled..
 */
public class CancellationToken {
    private final CancellationTokenSource tokenSource;
    private AtomicBoolean cancellationRequested;

    public CancellationToken(CancellationTokenSource source) {
        this.tokenSource = source;
        this.cancellationRequested = new AtomicBoolean(false);
    }

    public void cancel() {
        this.cancellationRequested.set(true);
    }

    /**
     * @return true if the cancellation was requested from the source.
     */
    public boolean isCancellationRequested() {
        return tokenSource.isCancellationRequested() || this.cancellationRequested.get();
    }
}
