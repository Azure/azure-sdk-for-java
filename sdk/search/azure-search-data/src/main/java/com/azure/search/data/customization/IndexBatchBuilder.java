// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.customization;

import com.azure.search.data.SearchIndexBatchBuilder;
import com.azure.search.data.common.jsonwrapper.JsonWrapper;
import com.azure.search.data.common.jsonwrapper.api.JsonApi;
import com.azure.search.data.common.jsonwrapper.jacksonwrapper.JacksonDeserializer;
import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexActionType;
import com.azure.search.data.generated.models.IndexBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class IndexBatchBuilder implements SearchIndexBatchBuilder {
    private JsonApi jsonApi;
    private List<IndexAction> indexActions;

    /**
     * Package private constructor to be used by {@link SearchIndexClientImpl} or {@link SearchIndexAsyncClientImpl}
     */
    IndexBatchBuilder() {
        jsonApi = JsonWrapper.newInstance(JacksonDeserializer.class);
        jsonApi.configureTimezone();
        indexActions = new ArrayList<>();
    }

    /**
     * Adds an Upload IndexAction to the IndexAction chain for a document.
     *
     * @param document The document to be uploaded.
     * @param <T>      The type of object to serialize
     * @return IndexBatchBuilder with the desired actions.
     */
    public <T> IndexBatchBuilder upload(T document) {
        appendDocumentAction(document, IndexActionType.UPLOAD);
        return this;
    }

    /**
     * Adds Upload IndexActions to the IndexAction chain for a collection of documents.
     *
     * @param documents The document collection to be uploaded.
     * @param <T>       The type of object to serialize
     * @return IndexBatchBuilder with the desired actions.
     */
    public <T> IndexBatchBuilder upload(List<T> documents) {
        appendDocumentAction(documents, IndexActionType.UPLOAD);
        return this;
    }

    /**
     * Adds a Delete IndexAction to the IndexAction chain for a document.
     *
     * @param document The document to be uploaded.
     * @param <T>      The type of object to serialize
     * @return IndexBatchBuilder with the desired actions.
     */
    public <T> IndexBatchBuilder delete(T document) {
        appendDocumentAction(document, IndexActionType.DELETE);
        return this;
    }

    /**
     * Adds Delete IndexActions to the IndexAction chain for a collection of documents.
     *
     * @param documents The document collection to be uploaded.
     * @param <T>       The type of object to serialize
     * @return IndexBatchBuilder with the desired actions.
     */
    public <T> IndexBatchBuilder delete(List<T> documents) {
        appendDocumentAction(documents, IndexActionType.DELETE);
        return this;
    }

    /**
     * Adds a Merge IndexAction to the IndexAction chain for a document.
     *
     * @param document The document to be uploaded.
     * @param <T>      The type of object to serialize
     * @return IndexBatchBuilder with the desired actions.
     */
    public <T> IndexBatchBuilder merge(T document) {
        appendDocumentAction(document, IndexActionType.MERGE);
        return this;
    }

    /**
     * Adds Merge IndexActions to the IndexAction chain for a collection of documents.
     *
     * @param documents The document collection to be uploaded.
     * @param <T>       The type of object to serialize
     * @return IndexBatchBuilder with the desired actions.
     */
    public <T> IndexBatchBuilder merge(List<T> documents) {
        appendDocumentAction(documents, IndexActionType.MERGE);
        return this;
    }

    /**
     * Adds a Merge or Upload IndexAction to the IndexAction chain for a document.
     *
     * @param document The document to be uploaded.
     * @param <T>      The type of object to serialize
     * @return IndexBatchBuilder with the desired actions.
     */
    public <T> IndexBatchBuilder mergeOrUpload(T document) {
        appendDocumentAction(document, IndexActionType.MERGE_OR_UPLOAD);
        return this;
    }

    /**
     * Adds Merge or Upload IndexActions to the IndexAction chain for a collection of documents.
     *
     * @param documents The document collection to be uploaded.
     * @param <T>       The type of object to serialize
     * @return IndexBatchBuilder with the desired actions.
     */
    public <T> IndexBatchBuilder mergeOrUpload(List<T> documents) {
        appendDocumentAction(documents, IndexActionType.MERGE_OR_UPLOAD);
        return this;
    }

    private <T> void appendDocumentAction(T document, IndexActionType actionType) {
        this.indexActions.add(new IndexAction().
            actionType(actionType).
            additionalProperties(entityToMap(document)));
    }

    private <T> void appendDocumentAction(List<T> documents, IndexActionType actionType) {
        this.indexActions.addAll(documents.stream()
            .map(doc -> new IndexAction()
                .actionType(actionType)
                .additionalProperties(entityToMap(doc))).collect(Collectors.toList()));
    }

    private <T> Map<String, Object> entityToMap(T entity) {
        return this.jsonApi.convertObjectToType(entity, Map.class);
    }

    /**
     * Creates an IndexBatch from the chained set of actions.
     *
     * @return An IndexBatch with the desired actions.
     */
    public IndexBatch batch() {
        return new IndexBatch().actions(this.indexActions);
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
