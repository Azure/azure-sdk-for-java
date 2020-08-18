// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.SearchIndexStatistics;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.GetIndexStatisticsResult} and
 * {@link SearchIndexStatistics}.
 */
public final class GetIndexStatisticsResultConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.GetIndexStatisticsResult} to
     * {@link SearchIndexStatistics}.
     */
    public static SearchIndexStatistics map(com.azure.search.documents.indexes.implementation.models.GetIndexStatisticsResult obj) {
        if (obj == null) {
            return null;
        }
        return new SearchIndexStatistics(obj.getDocumentCount(), obj.getStorageSize());
    }

    /**
     * Maps from {@link SearchIndexStatistics} to
     * {@link com.azure.search.documents.indexes.implementation.models.GetIndexStatisticsResult}.
     */
    public static com.azure.search.documents.indexes.implementation.models.GetIndexStatisticsResult map(SearchIndexStatistics obj) {
        if (obj == null) {
            return null;
        }

        return new com.azure.search.documents.indexes.implementation.models.GetIndexStatisticsResult(
            obj.getDocumentCount(), obj.getStorageSize());
    }

    private GetIndexStatisticsResultConverter() {
    }
}
