// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.options;

import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.models.IndexAction;

import java.util.function.Consumer;

/**
 * Options passed when {@link SearchClientBuilder.SearchIndexingBufferedSenderBuilder#onActionSent(Consumer)} is called.
 */
public final class OnActionSentOptions {
    private final IndexAction indexAction;

    /**
     * Creates a new OnActionSentOptions object.
     *
     * @param indexAction Action that was sent.
     */
    public OnActionSentOptions(IndexAction indexAction) {
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
