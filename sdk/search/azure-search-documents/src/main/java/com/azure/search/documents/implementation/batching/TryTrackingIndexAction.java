// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.batching;

import com.azure.search.documents.models.IndexAction;

/**
 * Model class that tracks the number of times an IndexAction has tried to be indexed.
 */
final class TryTrackingIndexAction<T> {
    private final IndexAction<T> action;
    private final String key;

    private int tryCount = 0;

    TryTrackingIndexAction(IndexAction<T> action, String key) {
        this.action = action;
        this.key = key;
    }

    public IndexAction<T> getAction() {
        return action;
    }

    public String getKey() {
        return key;
    }

    public int getTryCount() {
        return tryCount;
    }

    public void incrementTryCount() {
        tryCount++;
    }
}
