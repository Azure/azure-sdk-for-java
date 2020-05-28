// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexBatchBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Contains a batch of document write actions to send to the index.
 */
@Fluent
public class IndexDocumentsBatch<T> extends IndexBatchBase<T> {
    /**
     * Constructor
     */
    public IndexDocumentsBatch() {
        this.actions(new ArrayList<>());
    }

    /**
     * Set the actions property: The actions in the batch.
     *
     * @param actions the actions value to set.
     * @return the IndexBatch object itself.
     */
    public IndexDocumentsBatch<T> actions(List<IndexAction<T>> actions) {
        return (IndexDocumentsBatch<T>) super.setActions(actions);
    }


    /**
     * Adds an Upload IndexAction to the IndexAction chain for a document.
     *
     * @param documents The documents to be uploaded.
     * @return IndexBatch with the desired actions added.
     */
    @SuppressWarnings("unchecked")
    public IndexDocumentsBatch<T> addUploadActions(T... documents) {
        addDocumentActions(Arrays.asList(documents), IndexActionType.UPLOAD);
        return this;
    }

    /**
     * Adds Upload IndexActions to the IndexAction chain for a collection of documents.
     *
     * @param documents The document collection to be uploaded.
     * @return IndexBatch with the desired actions added.
     */
    public IndexDocumentsBatch<T> addUploadActions(Iterable<T> documents) {
        addDocumentActions(documents, IndexActionType.UPLOAD);
        return this;
    }

    /**
     * Adds a Delete IndexAction to the IndexAction chain for a document.
     *
     * @param documents The documents to be deleted.
     * @return IndexBatch with the desired actions added.
     */
    @SuppressWarnings("unchecked")
    public IndexDocumentsBatch<T> addDeleteActions(T... documents) {
        addDocumentActions(Arrays.asList(documents), IndexActionType.DELETE);
        return this;
    }

    /**
     * Adds Delete IndexActions to the IndexAction chain for a collection of documents.
     *
     * @param documents The document collection to be deleted.
     * @return IndexBatch with the desired actions added.
     */
    public IndexDocumentsBatch<T> addDeleteActions(Iterable<T> documents) {
        addDocumentActions(documents, IndexActionType.DELETE);
        return this;
    }

    /**
     * Adds Delete IndexActions to the IndexAction chain for a collection of documents.
     *
     * @param keyName The name of the key field that uniquely identifies documents in the index.
     * @param keyValues The keys of the documents to delete.
     * @return IndexBatch with the desired actions added.
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
     * Adds Delete IndexActions to the IndexAction chain for a collection of documents.
     *
     * @param keyName The name of the key field that uniquely identifies documents in the index.
     * @param keyValues The keys of the documents to delete.
     * @return IndexBatch with the desired actions added.
     */
    public IndexDocumentsBatch<T> addDeleteActions(String keyName, String... keyValues) {
        return this.addDeleteActions(keyName, Arrays.asList(keyValues));
    }

    /**
     * Adds a Merge IndexAction to the IndexAction chain for a document.
     *
     * @param documents The documents to be merged.
     * @return IndexBatch with the desired actions added.
     */
    @SuppressWarnings("unchecked")
    public IndexDocumentsBatch<T> addMergeActions(T... documents) {
        addDocumentActions(Arrays.asList(documents), IndexActionType.MERGE);
        return this;
    }

    /**
     * Adds Merge IndexActions to the IndexAction chain for a collection of documents.
     *
     * @param documents The document collection to be merged.
     * @return IndexBatch with the desired actions added.
     */
    public IndexDocumentsBatch<T> addMergeActions(Iterable<T> documents) {
        addDocumentActions(documents, IndexActionType.MERGE);
        return this;
    }

    /**
     * Adds a Merge or Upload IndexAction to the IndexAction chain for a document.
     *
     * @param documents The documents to be merged or uploaded.
     * @return IndexBatch with the desired actions added.
     */
    @SuppressWarnings("unchecked")
    public IndexDocumentsBatch<T> addMergeOrUploadActions(T... documents) {
        addDocumentActions(Arrays.asList(documents), IndexActionType.MERGE_OR_UPLOAD);
        return this;
    }

    /**
     * Adds Merge or Upload IndexActions to the IndexAction chain for a collection of documents.
     *
     * @param documents The document collection to be merged or uploaded.
     * @return IndexBatch with the desired actions added.
     */
    public IndexDocumentsBatch<T> addMergeOrUploadActions(Iterable<T> documents) {
        addDocumentActions(documents, IndexActionType.MERGE_OR_UPLOAD);
        return this;
    }

    private void addDocumentActions(Iterable<T> documents, IndexActionType actionType) {
        documents.forEach(d -> {
            this.getActions().add(new IndexAction<T>()
                .setActionType(actionType)
                .setDocument(d));
        });
    }
}
