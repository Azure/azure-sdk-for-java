// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.models.IndexDocumentsResult;
import com.azure.search.documents.models.IndexingResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.IndexDocumentsResult} and
 * {@link IndexDocumentsResult}.
 */
public final class IndexDocumentsResultConverter {
    /**
     * Maps from {@link com.azure.search.documents.implementation.models.IndexDocumentsResult} to
     * {@link IndexDocumentsResult}.
     */
    public static IndexDocumentsResult map(com.azure.search.documents.implementation.models.IndexDocumentsResult obj) {
        if (obj == null) {
            return null;
        }

        List<IndexingResult> results = obj.getResults() == null ? null
            : obj.getResults().stream().map(IndexingResultConverter::map).collect(Collectors.toList());
        return new IndexDocumentsResult(results);
    }

    /**
     * Maps from {@link IndexDocumentsResult} to
     * {@link com.azure.search.documents.implementation.models.IndexDocumentsResult}.
     */
    public static com.azure.search.documents.implementation.models.IndexDocumentsResult map(IndexDocumentsResult obj) {
        if (obj == null) {
            return null;
        }

        List<com.azure.search.documents.implementation.models.IndexingResult> results = obj.getResults() == null
            ? null : obj.getResults().stream().map(IndexingResultConverter::map).collect(Collectors.toList());

        return new com.azure.search.documents.implementation.models.IndexDocumentsResult(results);
    }

    private IndexDocumentsResultConverter() {
    }
}
