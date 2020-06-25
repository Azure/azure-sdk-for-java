// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.indexes.models.IndexerExecutionResult;
import com.azure.search.documents.indexes.models.IndexerStatus;
import com.azure.search.documents.indexes.models.SearchIndexerLimits;
import com.azure.search.documents.indexes.models.SearchIndexerStatus;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.SearchIndexerStatus} and
 * {@link SearchIndexerStatus}.
 */
public final class SearchIndexerStatusConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.SearchIndexerStatus} to
     * {@link SearchIndexerStatus}.
     */
    public static SearchIndexerStatus map(com.azure.search.documents.indexes.implementation.models.SearchIndexerStatus obj) {
        if (obj == null) {
            return null;
        }

        IndexerStatus status = obj.getStatus() == null ? null : IndexerStatusConverter.map(obj.getStatus());

        List<IndexerExecutionResult> executionHistory = obj.getExecutionHistory() == null ? null
            : obj.getExecutionHistory().stream().map(IndexerExecutionResultConverter::map).collect(Collectors.toList());

        SearchIndexerLimits limits = obj.getLimits() == null ? null : SearchIndexerLimitsConverter.map(obj.getLimits());

        SearchIndexerStatus searchIndexerStatus = new SearchIndexerStatus(status, executionHistory, limits);

        if (obj.getLastResult() != null) {
            IndexerExecutionResult lastResult = IndexerExecutionResultConverter.map(obj.getLastResult());
            PrivateFieldAccessHelper.set(searchIndexerStatus, "lastResult", lastResult);
        }

        return searchIndexerStatus;
    }

    /**
     * Maps from {@link SearchIndexerStatus} to
     * {@link com.azure.search.documents.indexes.implementation.models.SearchIndexerStatus}.
     */
    public static com.azure.search.documents.indexes.implementation.models.SearchIndexerStatus map(SearchIndexerStatus obj) {
        if (obj == null) {
            return null;
        }

        com.azure.search.documents.indexes.implementation.models.IndexerStatus status =
            obj.getStatus() == null ? null
            : IndexerStatusConverter.map(obj.getStatus());

        List<com.azure.search.documents.indexes.implementation.models.IndexerExecutionResult> executionHistory =
            obj.getExecutionHistory() == null ? null
            : obj.getExecutionHistory().stream().map(IndexerExecutionResultConverter::map).collect(Collectors.toList());

        com.azure.search.documents.indexes.implementation.models.SearchIndexerLimits limits =
            obj.getLimits() == null ? null
            : SearchIndexerLimitsConverter.map(obj.getLimits());

        com.azure.search.documents.indexes.implementation.models.SearchIndexerStatus searchIndexerStatus =
            new com.azure.search.documents.indexes.implementation.models.SearchIndexerStatus(status, executionHistory,
                limits);

        if (obj.getLastResult() != null) {
            com.azure.search.documents.indexes.implementation.models.IndexerExecutionResult lastResult =
                IndexerExecutionResultConverter.map(obj.getLastResult());
            PrivateFieldAccessHelper.set(searchIndexerStatus, "lastResult", lastResult);
        }

        searchIndexerStatus.validate();
        return searchIndexerStatus;
    }

    private SearchIndexerStatusConverter() {
    }
}
