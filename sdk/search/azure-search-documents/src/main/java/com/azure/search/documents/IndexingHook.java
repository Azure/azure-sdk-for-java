// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.search.documents.models.IndexAction;

/**
 * This interface represents callback hooks that are triggered when {@link IndexAction IndexActions} are added, succeed,
 * fail, or are removed from the document indexing batch while using a {@link SearchBatchClient} or {@link
 * SearchBatchAsyncClient}.
 */
public interface IndexingHook {
    /**
     * Callback hook for when a document indexing action has been added to a batch queued.
     *
     * @param action The {@link IndexAction} that has been added to a batch queue.
     */
    void actionAdded(IndexAction<?> action);

    /**
     * Callback hook for when a document indexing action has successfully completed indexing.
     *
     * @param action The {@link IndexAction} that successfully completed indexing.
     */
    void actionSuccess(IndexAction<?> action);

    /**
     * Callback hook for when a document indexing action has failed to index and isn't retryable.
     *
     * @param action The {@link IndexAction} that failed to index and isn't retryable.
     */
    void actionError(IndexAction<?> action);

    /**
     * Callback hook for when a document indexing has been removed from a batching queue.
     * <p>
     * Actions are removed from the batch queue when they either succeed or fail indexing.
     *
     * @param action The {@link IndexAction} that has been removed from a batch queue.
     */
    void actionRemoved(IndexAction<?> action);
}
