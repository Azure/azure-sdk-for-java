// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.search.documents.SearchIndexingBufferedSenderBuilder;
import java.util.function.Consumer;

/**
 * Options passed when {@link SearchIndexingBufferedSenderBuilder#onActionSucceeded(Consumer)} is
 * called.
 */
public final class OnActionSucceededOptions {
    private final IndexAction indexAction;

    /**
     * Creates a new OnActionSucceededOptions object.
     *
     * @param indexAction The action that successfully completed indexing.
     */
    public OnActionSucceededOptions(IndexAction indexAction) {
        this.indexAction = indexAction;
    }

    /**
     * Gets the action.
     *
     * @return The action.
     */
    public IndexAction getIndexAction() {
        return indexAction;
    }
}
