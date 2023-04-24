// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.Context;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collection;

/**
 * This class provides a buffered sender that contains operations for conveniently indexing documents to an Azure Search
 * index.
 */
@ServiceClient(builder = SearchClientBuilder.class)
public final class SearchIndexingBufferedSender<T> {
    final SearchIndexingBufferedAsyncSender<T> client;

    SearchIndexingBufferedSender(SearchIndexingBufferedAsyncSender<T> client) {
        this.client = client;
    }

    /**
     * Gets the list of {@link IndexAction IndexActions} in the batch that are ready to be indexed.
     *
     * @return The list of {@link IndexAction IndexActions} in the batch that are ready to be indexed.
     */
    public Collection<IndexAction<T>> getActions() {
        return client.getActions();
    }

    /**
     * Gets the number of documents required in a batch for it to be flushed.
     * <p>
     * This configuration is only taken into account if auto flushing is enabled.
     *
     * @return The number of documents required before a flush is triggered.
     */
    int getBatchActionCount() {
        return client.getBatchActionCount();
    }

    /**
     * Adds upload document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be uploaded.
     */
    public void addUploadActions(Collection<T> documents) {
        addUploadActions(documents, null, Context.NONE);
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
    public void addUploadActions(Collection<T> documents, Duration timeout, Context context) {
        blockWithOptionalTimeout(client.createAndAddActions(documents, IndexActionType.UPLOAD, context), timeout);
    }

    /**
     * Adds delete document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be deleted.
     */
    public void addDeleteActions(Collection<T> documents) {
        addDeleteActions(documents, null, Context.NONE);
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
    public void addDeleteActions(Collection<T> documents, Duration timeout, Context context) {
        blockWithOptionalTimeout(client.createAndAddActions(documents, IndexActionType.DELETE, context), timeout);
    }

    /**
     * Adds merge document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be merged.
     */
    public void addMergeActions(Collection<T> documents) {
        addMergeActions(documents, null, Context.NONE);
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
    public void addMergeActions(Collection<T> documents, Duration timeout, Context context) {
        blockWithOptionalTimeout(client.createAndAddActions(documents, IndexActionType.MERGE, context), timeout);
    }

    /**
     * Adds merge or upload document actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param documents Documents to be merged or uploaded.
     */
    public void addMergeOrUploadActions(Collection<T> documents) {
        addMergeOrUploadActions(documents, null, Context.NONE);
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
    public void addMergeOrUploadActions(Collection<T> documents, Duration timeout, Context context) {
        blockWithOptionalTimeout(client.createAndAddActions(documents, IndexActionType.MERGE_OR_UPLOAD, context),
            timeout);
    }

    /**
     * Adds document index actions to the batch.
     * <p>
     * If the client is enabled for automatic batch sending, adding documents may trigger the batch to be sent for
     * indexing.
     *
     * @param actions Index actions.
     */
    public void addActions(Collection<IndexAction<T>> actions) {
        addActions(actions, null, Context.NONE);
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
    public void addActions(Collection<IndexAction<T>> actions, Duration timeout, Context context) {
        blockWithOptionalTimeout(client.addActions(actions, context), timeout);
    }

    /**
     * Sends the current batch of documents to be indexed.
     */
    public void flush() {
        flush(null, Context.NONE);
    }

    /**
     * Sends the current batch of documents to be indexed.
     *
     * @param timeout Duration before the operation times out.
     * @param context Additional context that is passed through the HTTP pipeline.
     */
    public void flush(Duration timeout, Context context) {
        blockWithOptionalTimeout(client.flush(context), timeout);
    }

    /**
     * Closes the buffered sender, any documents remaining in the batch will be sent to the Search index for indexing.
     * <p>
     * Once the buffered sender has been closed any attempts to add documents or flush it will cause an {@link
     * IllegalStateException} to be thrown.
     */
    public void close() {
        close(null, Context.NONE);
    }

    /**
     * Closes the buffered, any documents remaining in the batch sill be sent to the Search index for indexing.
     * <p>
     * Once the buffered sender has been closed any attempts to add documents or flush it will cause an {@link
     * IllegalStateException} to be thrown.
     *
     * @param timeout Duration before the operation times out.
     * @param context Additional context that is passed through the HTTP pipeline.
     */
    public void close(Duration timeout, Context context) {
        blockWithOptionalTimeout(client.close(context), timeout);
    }

    private static void blockWithOptionalTimeout(Mono<?> operation, Duration timeout) {
        if (timeout == null) {
            operation.block();
        } else {
            operation.block(timeout);
        }
    }
}
