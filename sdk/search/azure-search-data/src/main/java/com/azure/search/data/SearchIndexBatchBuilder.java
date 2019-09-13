// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data;


import com.azure.search.data.generated.models.IndexBatch;

import java.util.List;

/**
 * The public interface for SearchIndexBatchBuilder.
 */
public interface SearchIndexBatchBuilder {

    /**
     * Adds an Upload IndexAction to the IndexAction chain for a document.
     *
     * @param document The document to be uploaded.
     * @param <T> The type of object to serialize
     * @return SearchIndexBatchBuilder with the desired actions.
     */
    <T> SearchIndexBatchBuilder upload(T document);

    /**
     * Adds Upload IndexActions to the IndexAction chain for a collection of documents.
     *
     * @param documents The document collection to be uploaded.
     * @param <T> The type of object to serialize
     * @return SearchIndexBatchBuilder with the desired actions.
     */
    <T> SearchIndexBatchBuilder upload(List<T> documents);

    /**
     * Adds a Delete IndexAction to the IndexAction chain for a document.
     *
     * @param document The document to be uploaded.
     * @param <T> The type of object to serialize
     * @return SearchIndexBatchBuilder with the desired actions.
     */
    <T> SearchIndexBatchBuilder delete(T document);

    /**
     * Adds Delete IndexActions to the IndexAction chain for a collection of documents.
     *
     * @param documents The document collection to be uploaded.
     * @param <T> The type of object to serialize
     * @return SearchIndexBatchBuilder with the desired actions.
     */
    <T> SearchIndexBatchBuilder delete(List<T> documents);

    /**
     * Adds a Merge IndexAction to the IndexAction chain for a document.
     *
     * @param document The document to be uploaded.
     * @param <T> The type of object to serialize
     * @return SearchIndexBatchBuilder with the desired actions.
     */
    <T> SearchIndexBatchBuilder merge(T document);

    /**
     * Adds Merge IndexActions to the IndexAction chain for a collection of documents.
     *
     * @param documents The document collection to be uploaded.
     * @param <T> The type of object to serialize
     * @return SearchIndexBatchBuilder with the desired actions.
     */
    <T> SearchIndexBatchBuilder merge(List<T> documents);

    /**
     * Adds a Merge or Upload IndexAction to the IndexAction chain for a document.
     *
     * @param document The document to be uploaded.
     * @param <T> The type of object to serialize
     * @return IndexBatchBuilder with the desired actions.
     */
    <T> SearchIndexBatchBuilder mergeOrUpload(T document);

    /**
     * Adds Merge or Upload IndexActions to the IndexAction chain for a collection of documents.
     *
     * @param documents The document collection to be uploaded.
     * @param <T> The type of object to serialize
     * @return IndexBatchBuilder with the desired actions.
     */
    <T> SearchIndexBatchBuilder mergeOrUpload(List<T> documents);

    /**
     * Creates an IndexBatch from the chained set of actions.
     *
     * @return An IndexBatch with the desired actions.
     */
    IndexBatch batch();

    /**
     * Gets the size of the stored set of IndexActions in the BatchBuilder.
     *
     * @return Size of the internal IndexAction chain.
     */
    int size();
}
