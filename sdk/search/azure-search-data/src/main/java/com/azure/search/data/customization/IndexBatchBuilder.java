// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.customization;

import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexActionType;
import com.azure.search.data.generated.models.IndexBatch;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class IndexBatchBuilder<T> {
    private final List<IndexAction<T>> indexActions;

    /**
     * Public constructor with no parameters.
     */
    public IndexBatchBuilder() {
        indexActions = new ArrayList<>();
    }

    /**
     * Adds an Upload IndexAction to the IndexAction chain for a document.
     *
     * @param document The document to be uploaded.
     * @return IndexBatchBuilder with the desired actions.
     */
    public IndexBatchBuilder<T> upload(T document) {
        appendDocumentAction(document, IndexActionType.UPLOAD);
        return this;
    }

    /**
     * Adds Upload IndexActions to the IndexAction chain for a collection of documents.
     *
     * @param documents The document collection to be uploaded.
     * @return IndexBatchBuilder with the desired actions.
     */
    public IndexBatchBuilder<T> upload(List<T> documents) {
        appendDocumentAction(documents, IndexActionType.UPLOAD);
        return this;
    }

    /**
     * Adds a Delete IndexAction to the IndexAction chain for a document.
     *
     * @param document The document to be uploaded.
     * @return IndexBatchBuilder with the desired actions.
     */
    public IndexBatchBuilder<T> delete(T document) {
        appendDocumentAction(document, IndexActionType.DELETE);
        return this;
    }

    /**
     * Adds Delete IndexActions to the IndexAction chain for a collection of documents.
     *
     * @param documents The document collection to be uploaded.
     * @return IndexBatchBuilder with the desired actions.
     */
    public IndexBatchBuilder<T> delete(List<T> documents) {
        appendDocumentAction(documents, IndexActionType.DELETE);
        return this;
    }

    /**
     * Adds Delete IndexActions to the IndexAction chain for a collection of documents.
     *
     * @param keyName The name of the key field that uniquely identifies documents in the index.
     * @param keyValues The keys of the documents to delete.
     * @return IndexBatchBuilder with the desired actions.
     */
    public IndexBatchBuilder<T> delete(String keyName, List<String> keyValues) {
        for (String val : keyValues) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(keyName, val);
            this.indexActions.add(new IndexAction()
                .actionType(IndexActionType.DELETE)
                .document(map));
        }
        return this;
    }

    /**
     * Adds a Merge IndexAction to the IndexAction chain for a document.
     *
     * @param document The document to be uploaded.
     * @return IndexBatchBuilder with the desired actions.
     */
    public IndexBatchBuilder<T> merge(T document) {
        appendDocumentAction(document, IndexActionType.MERGE);
        return this;
    }

    /**
     * Adds Merge IndexActions to the IndexAction chain for a collection of documents.
     *
     * @param documents The document collection to be uploaded.
     * @return IndexBatchBuilder with the desired actions.
     */
    public IndexBatchBuilder<T> merge(List<T> documents) {
        appendDocumentAction(documents, IndexActionType.MERGE);
        return this;
    }

    /**
     * Adds a Merge or Upload IndexAction to the IndexAction chain for a document.
     *
     * @param document The document to be uploaded.
     * @return IndexBatchBuilder with the desired actions.
     */
    public IndexBatchBuilder<T> mergeOrUpload(T document) {
        appendDocumentAction(document, IndexActionType.MERGE_OR_UPLOAD);
        return this;
    }

    /**
     * Adds Merge or Upload IndexActions to the IndexAction chain for a collection of documents.
     *
     * @param documents The document collection to be uploaded.
     * @return IndexBatchBuilder with the desired actions.
     */
    public IndexBatchBuilder<T> mergeOrUpload(List<T> documents) {
        appendDocumentAction(documents, IndexActionType.MERGE_OR_UPLOAD);
        return this;
    }

    private void appendDocumentAction(T document, IndexActionType actionType) {
        this.indexActions.add(new IndexAction<T>().
            actionType(actionType).
            document(document));
    }

    private void appendDocumentAction(List<T> documents, IndexActionType actionType) {
        documents.forEach(d -> {
            this.indexActions.add(new IndexAction<T>()
                .actionType(actionType)
                .document(d));
        });
    }

    /**
     * Creates an IndexBatch from the chained set of actions.
     *
     * @return An IndexBatch with the desired actions.
     */
    public IndexBatch<T> build() {
        return new IndexBatch<T>().actions(this.indexActions);
    }

    /**
     * Gets the size of the stored set of IndexActions in the BatchBuilder.
     *
     * @return Size of the internal IndexAction chain.
     */
    public int size() {
        return this.indexActions.size();
    }
}
