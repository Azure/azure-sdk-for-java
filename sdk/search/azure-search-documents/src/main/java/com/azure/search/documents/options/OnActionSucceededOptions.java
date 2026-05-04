// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.options;

import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.models.IndexAction;

import java.util.function.Consumer;

/**
 * Options passed when {@link SearchClientBuilder.SearchIndexingBufferedSenderBuilder#onActionSucceeded(Consumer)} is
 * called.
 *
 * @param <T> Type of the document in the action.
 */
public final class OnActionSucceededOptions<T> {
    private final IndexAction<T> indexAction;

    /**
     * Creates a new OnActionSucceededOptions object.
     *
     * @param indexAction The action that successfully completed indexing.
     */
    public OnActionSucceededOptions(IndexAction<T> indexAction) {
        this.indexAction = indexAction;
    }

    /**
     * Gets the action.
     *
     * @return The action.
     */
    public IndexAction<T> getIndexAction() {
        return indexAction;
    }
}
