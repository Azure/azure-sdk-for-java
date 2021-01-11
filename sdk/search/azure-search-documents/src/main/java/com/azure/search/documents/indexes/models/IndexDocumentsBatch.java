// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexBatchBase;

import java.util.ArrayList;

/**
 * Contains a batch of document write actions to send to the index.
 */
@Fluent
public class IndexDocumentsBatch<T> extends IndexBatchBase<T> {
    /**
     * Constructor of {@link IndexDocumentsBatch}.
     */
    public IndexDocumentsBatch() {
        super(new ArrayList<>());
    }

    /**
     * Adds document index actions to the batch.
     *
     * @param actions Index actions.
     * @return The updated IndexDocumentsBatch object.
     */
    public IndexDocumentsBatch<T> addActions(Iterable<IndexAction<T>> actions) {
        actions.forEach(action -> this.getActions().add(action));
        return this;
    }

    /**
     * Adds upload document actions to the batch.
     *
     * @param documents Documents to be uploaded.
     * @return The updated IndexDocumentsBatch object.
     */
    public IndexDocumentsBatch<T> addUploadActions(Iterable<T> documents) {
        addDocumentActions(documents, IndexActionType.UPLOAD);
        return this;
    }

    /**
     * Adds document delete actions to the batch.
     *
     * @param documents Document to be deleted.
     * @return The updated IndexDocumentsBatch object.
     */
    public IndexDocumentsBatch<T> addDeleteActions(Iterable<T> documents) {
        addDocumentActions(documents, IndexActionType.DELETE);
        return this;
    }

    /**
     * Adds document delete actions based on key IDs to the batch.
     *
     * @param keyName The key field name.
     * @param keyValues Keys of the documents to delete.
     * @return The updated IndexDocumentsBatch object.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public IndexDocumentsBatch<T> addDeleteActions(String keyName, Iterable<String> keyValues) {
        for (String val : keyValues) {
            SearchDocument doc = new SearchDocument();
            doc.put(keyName, val);
            IndexAction indexAction = new IndexAction()
                .setActionType(IndexActionType.DELETE)
                .setDocument(doc);
            this.getActions().add(indexAction);
        }
        return this;
    }

    /**
     * Adds merge document actions to the batch.
     *
     * @param documents Documents to be merged.
     * @return The updated IndexDocumentsBatch object.
     */
    public IndexDocumentsBatch<T> addMergeActions(Iterable<T> documents) {
        addDocumentActions(documents, IndexActionType.MERGE);
        return this;
    }

    /**
     * Adds merge or upload document actions to the batch.
     *
     * @param documents Documents to be merged or uploaded.
     * @return The updated IndexDocumentsBatch object.
     */
    public IndexDocumentsBatch<T> addMergeOrUploadActions(Iterable<T> documents) {
        addDocumentActions(documents, IndexActionType.MERGE_OR_UPLOAD);
        return this;
    }

    private void addDocumentActions(Iterable<T> documents, IndexActionType actionType) {
        documents.forEach(d -> this.getActions().add(new IndexAction<T>()
            .setActionType(actionType)
            .setDocument(d)));
    }
}
