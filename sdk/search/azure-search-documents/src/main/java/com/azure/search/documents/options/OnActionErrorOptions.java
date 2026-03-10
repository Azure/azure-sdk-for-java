// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.options;

import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexingResult;

import java.util.function.Consumer;

/**
 * Options passed when {@link SearchClientBuilder.SearchIndexingBufferedSenderBuilder#onActionError(Consumer)} is
 * called.
 */
public final class OnActionErrorOptions {
    private final IndexAction action;

    private Throwable throwable;
    private IndexingResult indexingResult;

    /**
     * Creates a new OnActionErrorOptions object.
     *
     * @param action Action that failed with an error.
     */
    public OnActionErrorOptions(IndexAction action) {
        this.action = action;
    }

    /**
     * Gets the action.
     *
     * @return The action.
     */
    public IndexAction getAction() {
        return action;
    }

    /**
     * Sets the throwable that caused the action failed.
     *
     * @param throwable Throwable that caused the action to fail.
     * @return The updated OnActionErrorOptions object.
     */
    public OnActionErrorOptions setThrowable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    /**
     * Gets the throwable that caused the action to fail.
     *
     * @return The throwable that caused the action to fail.
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * Sets the indexing result of the action with a non-retryable status code.
     *
     * @param indexingResult The indexing result for the action.
     * @return The updated OnActionErrorOptions object.
     */
    public OnActionErrorOptions setIndexingResult(IndexingResult indexingResult) {
        this.indexingResult = indexingResult;
        return this;
    }

    /**
     * Gets the indexing result of the action.
     *
     * @return The indexing result of the action.
     */
    public IndexingResult getIndexingResult() {
        return indexingResult;
    }
}
