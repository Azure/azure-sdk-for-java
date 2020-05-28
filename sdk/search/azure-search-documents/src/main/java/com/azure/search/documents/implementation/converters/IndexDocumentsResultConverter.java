// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
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
        IndexDocumentsResult indexDocumentsResult = new IndexDocumentsResult();

        if (obj.getResults() != null) {
            List<IndexingResult> results =
                obj.getResults().stream().map(IndexingResultConverter::map).collect(Collectors.toList());
            PrivateFieldAccessHelper.set(indexDocumentsResult, "results", results);
        }
        return indexDocumentsResult;
    }

    /**
     * Maps from {@link IndexDocumentsResult} to
     * {@link com.azure.search.documents.implementation.models.IndexDocumentsResult}.
     */
    public static com.azure.search.documents.implementation.models.IndexDocumentsResult map(IndexDocumentsResult obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.IndexDocumentsResult indexDocumentsResult =
            new com.azure.search.documents.implementation.models.IndexDocumentsResult();

        if (obj.getResults() != null) {
            List<com.azure.search.documents.implementation.models.IndexingResult> results =
                obj.getResults().stream().map(IndexingResultConverter::map).collect(Collectors.toList());
            PrivateFieldAccessHelper.set(indexDocumentsResult, "results", results);
        }
        return indexDocumentsResult;
    }

    private IndexDocumentsResultConverter() {
    }
}
