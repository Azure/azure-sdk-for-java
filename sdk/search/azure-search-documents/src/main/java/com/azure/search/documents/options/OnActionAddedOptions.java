// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.options;

import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.models.IndexAction;

import java.util.function.Consumer;

/**
 * Options passed when {@link SearchClientBuilder.SearchIndexingBufferedSenderBuilder#onActionAdded(Consumer)} is
 * called.
 *
 * @param <T> Type of the document in the action.
 */
public final class OnActionAddedOptions<T> {
    private final IndexAction<T> action;

    /**
     * Creates a new OnActionAddedOptions object.
     *
     * @param action Action being added.
     */
    public OnActionAddedOptions(IndexAction<T> action) {
        this.action = action;
    }

    /**
     * Gets the action.
     *
     * @return The action.
     */
    public IndexAction<T> getAction() {
        return action;
    }
}
