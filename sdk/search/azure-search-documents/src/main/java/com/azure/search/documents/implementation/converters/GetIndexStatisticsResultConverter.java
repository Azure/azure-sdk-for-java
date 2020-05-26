// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.indexes.models.GetIndexStatisticsResult;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.GetIndexStatisticsResult} and
 * {@link GetIndexStatisticsResult}.
 */
public final class GetIndexStatisticsResultConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.GetIndexStatisticsResult} to
     * {@link GetIndexStatisticsResult}.
     */
    public static GetIndexStatisticsResult map(com.azure.search.documents.indexes.implementation.models.GetIndexStatisticsResult obj) {
        if (obj == null) {
            return null;
        }
        GetIndexStatisticsResult getIndexStatisticsResult = new GetIndexStatisticsResult();

        long documentCount = obj.getDocumentCount();
        PrivateFieldAccessHelper.set(getIndexStatisticsResult, "documentCount", documentCount);

        long storageSize = obj.getStorageSize();
        PrivateFieldAccessHelper.set(getIndexStatisticsResult, "storageSize", storageSize);
        return getIndexStatisticsResult;
    }

    /**
     * Maps from {@link GetIndexStatisticsResult} to
     * {@link com.azure.search.documents.indexes.implementation.models.GetIndexStatisticsResult}.
     */
    public static com.azure.search.documents.indexes.implementation.models.GetIndexStatisticsResult map(GetIndexStatisticsResult obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.GetIndexStatisticsResult getIndexStatisticsResult =
            new com.azure.search.documents.indexes.implementation.models.GetIndexStatisticsResult();

        long documentCount = obj.getDocumentCount();
        PrivateFieldAccessHelper.set(getIndexStatisticsResult, "documentCount", documentCount);

        long storageSize = obj.getStorageSize();
        PrivateFieldAccessHelper.set(getIndexStatisticsResult, "storageSize", storageSize);
        return getIndexStatisticsResult;
    }

    private GetIndexStatisticsResultConverter() {
    }
}
