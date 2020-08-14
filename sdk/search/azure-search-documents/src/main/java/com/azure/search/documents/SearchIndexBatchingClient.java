// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.search.documents.models.IndexAction;

import java.util.Collection;

/**
 *
 */
public class SearchIndexBatchingClient {
    private final SearchIndexBatchingAsyncClient client;

    SearchIndexBatchingClient(SearchIndexBatchingAsyncClient client) {
        this.client = client;
    }

    /**
     * Gets the list of {@link IndexAction IndexActions} in the batch that are ready to be indexed.
     *
     * @return The list of {@link IndexAction IndexActions} in the batch that are ready to be indexed.
     */
    public Collection<IndexAction<?>> getActions() {
        return client.getActions();
    }

    /**
     * Gets the list of {@link IndexAction IndexActions} in the batch that have been successfully indexed.
     *
     * @return The list of {@link IndexAction IndexActions} in the batch that have been successfully indexed.
     */
    public Collection<IndexAction<?>> getSucceededActions() {
        return client.getSucceededActions();
    }

    /**
     * Gets the list of {@link IndexAction IndexActions} in the batch that have failed indexing and aren't able to be
     * retried.
     *
     * @return The list of {@link IndexAction IndexActions} in the batch that have failed indexing and aren't able to be
     * retried.
     */
    public Collection<IndexAction<?>> getFailedActions() {
        return client.getFailedActions();
    }

    /**
     * Gets the batch size.
     *
     * @return The batch size.
     */
    public int getBatchSize() {
        return client.getBatchSize();
    }

    /**
     * Adds upload document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be uploaded.
     */
    public void addUploadActions(Collection<?> documents) {
        client.addUploadActions(documents).block();
    }

    /**
     * Adds delete document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be deleted.
     */
    public void addDeleteActions(Collection<?> documents) {
        client.addDeleteActions(documents).block();
    }

    /**
     * Adds merge document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be merged.
     */
    public void addMergeActions(Collection<?> documents) {
        client.addMergeActions(documents).block();
    }

    /**
     * Adds merge or upload document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be merged or uploaded.
     */
    public void addMergeOrUploadActions(Collection<?> documents) {
        client.addMergeOrUploadActions(documents).block();
    }

    /**
     * Adds document index actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param actions Index actions.
     */
    public void addActions(Collection<IndexAction<?>> actions) {
        client.addActions(actions).block();
    }

    /**
     * Sends the current batch of documents to be indexed.
     *
     * @param throwOnAnyFailure Flag indicating if the batch should raise an error if any documents in the batch fail to
     * index.
     */
    public void flush(boolean throwOnAnyFailure) {
        client.flush(throwOnAnyFailure);
    }

    /**
     * Closes the batch, any documents remaining in the batch will be sent to the Search index for indexing.
     */
    public void close() {
        client.close();
    }
}
