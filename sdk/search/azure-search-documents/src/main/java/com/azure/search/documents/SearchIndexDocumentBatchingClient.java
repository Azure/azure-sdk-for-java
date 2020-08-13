// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.search.documents.models.IndexAction;

import java.util.Collection;

/**
 *
 */
public class SearchIndexDocumentBatchingClient {
    private final SearchIndexDocumentBatchingAsyncClient client;

    SearchIndexDocumentBatchingClient(SearchIndexDocumentBatchingAsyncClient client) {
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

    public void addUploadActions(Collection<?> documents) {
        client.addUploadActions(documents).block();
    }

    public void addDeleteActions(Collection<?> documents) {
        client.addDeleteActions(documents).block();
    }

    public void addMergeActions(Collection<?> documents) {
        client.addMergeActions(documents).block();
    }

    public void addMergeOrUploadActions(Collection<?> documents) {
        client.addMergeOrUploadActions(documents).block();
    }

    public void addActions(Collection<IndexAction<?>> actions) {
        client.addActions(actions).block();
    }

    /**
     * Sends the current batch of documents to be indexed.
     *
     * @param raiseError Flag indicating if the batch should raise an error if any documents in the batch fail to index.
     */
    public void flush(boolean raiseError) {
        client.flush(raiseError);
    }

    /**
     * Closes the batch, any documents remaining in the batch will be sent to the Search index for indexing.
     */
    public void close() {
        client.close();
    }
}
