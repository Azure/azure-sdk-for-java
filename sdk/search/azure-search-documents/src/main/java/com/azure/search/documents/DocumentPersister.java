// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.search.documents.models.IndexAction;

import java.util.Collection;

/**
 * Handles persisting documents being handled by a {@link SearchIndexDocumentBatchingClient} or {@link
 * SearchIndexDocumentBatchingAsyncClient}. APIs are called when a document is added, completes indexing, or fails to
 * index. This allows documents to be stored persistently in case the batching client is closed while documents remain
 * or in the case of an application failure.
 */
public interface DocumentPersister {
    /**
     * Persists {@link IndexAction IndexActions} that have been queued for indexing.
     *
     * @param actions {@link IndexAction IndexActions} that have been added to a batch queue.
     */
    void addQueuedActions(Collection<IndexAction<?>> actions);

    /**
     * Persists an {@link IndexAction} that has successfully completed indexing.
     *
     * @param action An {@link IndexAction} that successfully completed indexing.
     */
    void addSucceededAction(IndexAction<?> action);

    /**
     * Persists an {@link IndexAction} that has failed to index and isn't retryable.
     *
     * @param action An {@link IndexAction} that failed to index and isn't retryable.
     */
    void addFailedAction(IndexAction<?> action);

    /**
     * Removes an {@link IndexAction} that has either successfully or unsuccessfully been indexed.
     * <p>
     * {@link IndexAction IndexActions} that failed to index but are retryable won't be removed.
     *
     * @param action An {@link IndexAction} that has either successfully or unsuccessfully completed indexing.
     */
    void removeQueuedAction(IndexAction<?> action);
}
