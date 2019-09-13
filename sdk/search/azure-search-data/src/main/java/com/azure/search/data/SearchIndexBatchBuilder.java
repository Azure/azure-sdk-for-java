// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data;


import com.azure.search.data.generated.models.IndexBatch;

import java.util.List;

/**
 * The public interface for SearchIndexBatchBuilder.
 */
public interface SearchIndexBatchBuilder {

    <T> SearchIndexBatchBuilder upload(T document);

    <T> SearchIndexBatchBuilder upload(List<T> documents);

    <T> SearchIndexBatchBuilder delete(T document);

    <T> SearchIndexBatchBuilder delete(List<T> documents);

    <T> SearchIndexBatchBuilder merge(T document);

    <T> SearchIndexBatchBuilder merge(List<T> documents);

    <T> SearchIndexBatchBuilder mergeOrUpload(T document);

    <T> SearchIndexBatchBuilder mergeOrUpload(List<T> documents);

    IndexBatch batch();

    int size();
}
