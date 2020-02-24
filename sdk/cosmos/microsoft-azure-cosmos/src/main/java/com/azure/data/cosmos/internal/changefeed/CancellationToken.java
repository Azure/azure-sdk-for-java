// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

/**
 * Propagates notification that operations should be canceled..
 */
public class CancellationToken {
    private final CancellationTokenSource tokenSource;

    public CancellationToken(CancellationTokenSource source) {
        this.tokenSource = source;
    }

    /**
     * @return true if the cancellation was requested from the source.
     */
    public boolean isCancellationRequested() {
        return tokenSource.isCancellationRequested();
    }
}
