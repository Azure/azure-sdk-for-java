// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.models.IndexerExecutionResult;
import com.azure.search.documents.models.IndexerStatus;
import com.azure.search.documents.models.SearchIndexerLimits;
import com.azure.search.documents.models.SearchIndexerStatus;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.SearchIndexerStatus} and
 * {@link SearchIndexerStatus}.
 */
public final class SearchIndexerStatusConverter {
    /**
     * Maps from {@link com.azure.search.documents.implementation.models.SearchIndexerStatus} to
     * {@link SearchIndexerStatus}.
     */
    public static SearchIndexerStatus map(com.azure.search.documents.implementation.models.SearchIndexerStatus obj) {
        if (obj == null) {
            return null;
        }
        SearchIndexerStatus searchIndexerStatus = new SearchIndexerStatus();

        if (obj.getLastResult() != null) {
            IndexerExecutionResult lastResult = IndexerExecutionResultConverter.map(obj.getLastResult());
            PrivateFieldAccessHelper.set(searchIndexerStatus, "lastResult", lastResult);
        }

        if (obj.getExecutionHistory() != null) {
            List<IndexerExecutionResult> executionHistory =
                obj.getExecutionHistory().stream().map(IndexerExecutionResultConverter::map).collect(Collectors.toList());
            PrivateFieldAccessHelper.set(searchIndexerStatus, "executionHistory", executionHistory);
        }

        if (obj.getLimits() != null) {
            SearchIndexerLimits limits = SearchIndexerLimitsConverter.map(obj.getLimits());
            PrivateFieldAccessHelper.set(searchIndexerStatus, "limits", limits);
        }

        if (obj.getStatus() != null) {
            IndexerStatus status = IndexerStatusConverter.map(obj.getStatus());
            PrivateFieldAccessHelper.set(searchIndexerStatus, "status", status);
        }
        return searchIndexerStatus;
    }

    /**
     * Maps from {@link SearchIndexerStatus} to
     * {@link com.azure.search.documents.implementation.models.SearchIndexerStatus}.
     */
    public static com.azure.search.documents.implementation.models.SearchIndexerStatus map(SearchIndexerStatus obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.SearchIndexerStatus searchIndexerStatus =
            new com.azure.search.documents.implementation.models.SearchIndexerStatus();

        if (obj.getLastResult() != null) {
            com.azure.search.documents.implementation.models.IndexerExecutionResult lastResult =
                IndexerExecutionResultConverter.map(obj.getLastResult());
            PrivateFieldAccessHelper.set(searchIndexerStatus, "lastResult", lastResult);
        }

        if (obj.getExecutionHistory() != null) {
            List<com.azure.search.documents.implementation.models.IndexerExecutionResult> executionHistory =
                obj.getExecutionHistory().stream().map(IndexerExecutionResultConverter::map).collect(Collectors.toList());
            PrivateFieldAccessHelper.set(searchIndexerStatus, "executionHistory", executionHistory);
        }

        if (obj.getLimits() != null) {
            com.azure.search.documents.implementation.models.SearchIndexerLimits limits =
                SearchIndexerLimitsConverter.map(obj.getLimits());
            PrivateFieldAccessHelper.set(searchIndexerStatus, "limits", limits);
        }

        if (obj.getStatus() != null) {
            com.azure.search.documents.implementation.models.IndexerStatus status =
                IndexerStatusConverter.map(obj.getStatus());
            PrivateFieldAccessHelper.set(searchIndexerStatus, "status", status);
        }
        return searchIndexerStatus;
    }

    private SearchIndexerStatusConverter() {
    }
}
