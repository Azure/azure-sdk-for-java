// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * Contains a batch of document write actions to send to the index.
 */
@Fluent
public class IndexBatchBase<T> {
    /*
     * The actions in the batch.
     */
    private final List<IndexAction<T>> actions;

    /**
     * Constructor of {@link IndexBatchBase}
     * @param actions The actions in the batch.
     */
    public IndexBatchBase(List<IndexAction<T>> actions) {
        this.actions = actions;
    }

    /**
     * Get the actions property: The actions in the batch.
     *
     * @return the actions value.
     */
    public List<IndexAction<T>> getActions() {
        return this.actions;
    }
}
