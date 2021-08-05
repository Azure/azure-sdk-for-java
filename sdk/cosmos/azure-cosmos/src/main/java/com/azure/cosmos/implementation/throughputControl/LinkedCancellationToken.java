// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class LinkedCancellationToken {
    private final List<LinkedCancellationTokenSource> childTokenSourceList;
    private final LinkedCancellationTokenSource tokenSource;
    private final AtomicBoolean cancellationRequested;

    public LinkedCancellationToken(LinkedCancellationTokenSource tokenSource) {
        this.childTokenSourceList = new ArrayList<>();
        this.tokenSource = tokenSource;
        this.cancellationRequested = new AtomicBoolean();
    }

    public void register(LinkedCancellationTokenSource childTokenSource) {
        synchronized (this) {
            if (this.cancellationRequested.get()) {
                throw new IllegalStateException("The cancellation token has been cancelled");
            }

            this.childTokenSourceList.add(childTokenSource);
        }
    }

    public void cancel() {
        synchronized (this) {
            if (this.cancellationRequested.compareAndSet(false, true)) {
                for (LinkedCancellationTokenSource childTokenSource : this.childTokenSourceList) {
                    childTokenSource.close();
                }

                childTokenSourceList.clear();
            }
        }
    }

    public boolean isCancellationRequested() {
        return this.cancellationRequested.get()
            || this.tokenSource.isClosed();
    }
}
