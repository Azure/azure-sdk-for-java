// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.models;

import com.azure.core.annotation.Fluent;
import com.azure.search.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Contains a batch of document write actions to send to the index.
 */
@Fluent
public class IndexBatch<T> extends IndexBatchImpl<T> {
    /**
     * Constructor
     */
    public IndexBatch() {
        this.actions(new ArrayList<>());
    }

    /**
     * Set the actions property: The actions in the batch.
     *
     * @param actions the actions value to set.
     * @return the IndexBatch object itself.
     */
    public IndexBatch<T> actions(List<IndexAction<T>> actions) {
        return (IndexBatch<T>) super.setActions(actions);
    }


    /**
     * Adds an Upload IndexAction to the IndexAction chain for a document.
     *
     * @param documents The documents to be uploaded.
     * @return IndexBatch with the desired actions added.
     */
    @SuppressWarnings("unchecked")
    public IndexBatch<T> addUploadAction(T... documents) {
        addDocumentAction(Arrays.asList(documents), IndexActionType.UPLOAD);
        return this;
    }

    /**
     * Adds Upload IndexActions to the IndexAction chain for a collection of documents.
     *
     * @param documents The document collection to be uploaded.
     * @return IndexBatch with the desired actions added.
     */
    public IndexBatch<T> addUploadAction(Iterable<T> documents) {
        addDocumentAction(documents, IndexActionType.UPLOAD);
        return this;
    }

    /**
     * Adds a Delete IndexAction to the IndexAction chain for a document.
     *
     * @param documents The documents to be deleted.
     * @return IndexBatch with the desired actions added.
     */
    @SuppressWarnings("unchecked")
    public IndexBatch<T> addDeleteAction(T... documents) {
        addDocumentAction(Arrays.asList(documents), IndexActionType.DELETE);
        return this;
    }

    /**
     * Adds Delete IndexActions to the IndexAction chain for a collection of documents.
     *
     * @param documents The document collection to be deleted.
     * @return IndexBatch with the desired actions added.
     */
    public IndexBatch<T> addDeleteAction(Iterable<T> documents) {
        addDocumentAction(documents, IndexActionType.DELETE);
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
    public IndexBatch<T> addDeleteAction(String keyName, Iterable<String> keyValues) {
        for (String val : keyValues) {
            Document doc = new Document();
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
    public IndexBatch<T> addDeleteAction(String keyName, String... keyValues) {
        return this.addDeleteAction(keyName, Arrays.asList(keyValues));
    }

    /**
     * Adds a Merge IndexAction to the IndexAction chain for a document.
     *
     * @param documents The documents to be merged.
     * @return IndexBatch with the desired actions added.
     */
    @SuppressWarnings("unchecked")
    public IndexBatch<T> addMergeAction(T... documents) {
        addDocumentAction(Arrays.asList(documents), IndexActionType.MERGE);
        return this;
    }

    /**
     * Adds Merge IndexActions to the IndexAction chain for a collection of documents.
     *
     * @param documents The document collection to be merged.
     * @return IndexBatch with the desired actions added.
     */
    public IndexBatch<T> addMergeAction(Iterable<T> documents) {
        addDocumentAction(documents, IndexActionType.MERGE);
        return this;
    }

    /**
     * Adds a Merge or Upload IndexAction to the IndexAction chain for a document.
     *
     * @param documents The documents to be merged or uploaded.
     * @return IndexBatch with the desired actions added.
     */
    @SuppressWarnings("unchecked")
    public IndexBatch<T> addMergeOrUploadAction(T... documents) {
        addDocumentAction(Arrays.asList(documents), IndexActionType.MERGE_OR_UPLOAD);
        return this;
    }

    /**
     * Adds Merge or Upload IndexActions to the IndexAction chain for a collection of documents.
     *
     * @param documents The document collection to be merged or uploaded.
     * @return IndexBatch with the desired actions added.
     */
    public IndexBatch<T> addMergeOrUploadAction(Iterable<T> documents) {
        addDocumentAction(documents, IndexActionType.MERGE_OR_UPLOAD);
        return this;
    }

    private void addDocumentAction(Iterable<T> documents, IndexActionType actionType) {
        documents.forEach(d -> {
            this.getActions().add(new IndexAction<T>()
                .setActionType(actionType)
                .setDocument(d));
        });
    }
}
