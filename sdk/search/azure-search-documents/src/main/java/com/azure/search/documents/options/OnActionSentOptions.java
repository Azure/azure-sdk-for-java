// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.options;

import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.models.IndexAction;

import java.util.function.Consumer;

/**
 * Options passed when {@link SearchClientBuilder.SearchIndexingBufferedSenderBuilder#onActionSent(Consumer)} is called.
 *
 * @param <T> Type of the document in the action.
 */
public final class OnActionSentOptions<T> {
    private final IndexAction<T> indexAction;

    /**
     * Creates a new OnActionSentOptions object.
     *
     * @param indexAction Action that was sent.
     */
    public OnActionSentOptions(IndexAction<T> indexAction) {
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
