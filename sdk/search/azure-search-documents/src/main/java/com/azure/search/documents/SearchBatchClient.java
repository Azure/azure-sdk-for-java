// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.util.Context;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;

import java.time.Duration;
import java.util.Collection;

/**
 * This class provides a client that contains operations for conveniently indexing documents to an Azure Search index.
 *
 * @see SearchBatchClientBuilder
 */
public final class SearchBatchClient {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofDays(1);

    private final SearchBatchAsyncClient client;

    SearchBatchClient(SearchBatchAsyncClient client) {
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
        addUploadActions(documents, DEFAULT_TIMEOUT, Context.NONE);
    }

    /**
     * Adds upload document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be uploaded.
     * @param timeout Duration before the operation times out.
     * @param context Additional context that is passed through the HTTP pipeline.
     */
    public void addUploadActions(Collection<?> documents, Duration timeout, Context context) {
        client.createAndAddActions(documents, IndexActionType.UPLOAD, context).block(timeout);
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
        addDeleteActions(documents, DEFAULT_TIMEOUT, Context.NONE);
    }

    /**
     * Adds delete document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be deleted.
     * @param timeout Duration before the operation times out.
     * @param context Additional context that is passed through the HTTP pipeline.
     */
    public void addDeleteActions(Collection<?> documents, Duration timeout, Context context) {
        client.createAndAddActions(documents, IndexActionType.DELETE, context).block(timeout);
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
        addMergeActions(documents, DEFAULT_TIMEOUT, Context.NONE);
    }

    /**
     * Adds merge document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be merged.
     * @param timeout Duration before the operation times out.
     * @param context Additional context that is passed through the HTTP pipeline.
     */
    public void addMergeActions(Collection<?> documents, Duration timeout, Context context) {
        client.createAndAddActions(documents, IndexActionType.MERGE, context).block(timeout);
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
        addMergeOrUploadActions(documents, DEFAULT_TIMEOUT, Context.NONE);
    }

    /**
     * Adds merge or upload document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be merged or uploaded.
     * @param timeout Duration before the operation times out.
     * @param context Additional context that is passed through the HTTP pipeline.
     */
    public void addMergeOrUploadActions(Collection<?> documents, Duration timeout, Context context) {
        client.createAndAddActions(documents, IndexActionType.MERGE_OR_UPLOAD, context).block(timeout);
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
        addActions(actions, DEFAULT_TIMEOUT, Context.NONE);
    }

    /**
     * Adds document index actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param actions Index actions.
     * @param timeout Duration before the operation times out.
     * @param context Additional context that is passed through the HTTP pipeline.
     */
    public void addActions(Collection<IndexAction<?>> actions, Duration timeout, Context context) {
        client.addActions(actions, context).block(timeout);
    }

    /**
     * Sends the current batch of documents to be indexed.
     */
    public void flush() {
        flush(DEFAULT_TIMEOUT, Context.NONE);
    }

    /**
     * Sends the current batch of documents to be indexed.
     *
     * @param timeout Duration before the operation times out.
     * @param context Additional context that is passed through the HTTP pipeline.
     */
    public void flush(Duration timeout, Context context) {
        client.flush(context).block(timeout);
    }

    /**
     * Closes the batch, any documents remaining in the batch will be sent to the Search index for indexing.
     */
    public void close() {
        close(DEFAULT_TIMEOUT, Context.NONE);
    }

    /**
     * Closes the batch, any documents remaining in the batch sill be sent to the Search index for indexing.
     *
     * @param timeout Duration before the operation times out.
     * @param context Additional context that is passed through the HTTP pipeline.
     */
    public void close(Duration timeout, Context context) {
        client.close(context).block(timeout);
    }
}
