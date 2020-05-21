// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.models.GetIndexStatisticsResult;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.GetIndexStatisticsResult} and
 * {@link GetIndexStatisticsResult}.
 */
public final class GetIndexStatisticsResultConverter {
    private static final ClientLogger LOGGER = new ClientLogger(GetIndexStatisticsResultConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.GetIndexStatisticsResult} to
     * {@link GetIndexStatisticsResult}.
     */
    public static GetIndexStatisticsResult map(com.azure.search.documents.implementation.models.GetIndexStatisticsResult obj) {
        if (obj == null) {
            return null;
        }
        GetIndexStatisticsResult getIndexStatisticsResult = new GetIndexStatisticsResult();

        long _documentCount = obj.getDocumentCount();
        PrivateFieldAccessHelper.set(getIndexStatisticsResult, "documentCount", _documentCount);

        long _storageSize = obj.getStorageSize();
        PrivateFieldAccessHelper.set(getIndexStatisticsResult, "storageSize", _storageSize);
        return getIndexStatisticsResult;
    }

    /**
     * Maps from {@link GetIndexStatisticsResult} to
     * {@link com.azure.search.documents.implementation.models.GetIndexStatisticsResult}.
     */
    public static com.azure.search.documents.implementation.models.GetIndexStatisticsResult map(GetIndexStatisticsResult obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.GetIndexStatisticsResult getIndexStatisticsResult =
            new com.azure.search.documents.implementation.models.GetIndexStatisticsResult();

        long _documentCount = obj.getDocumentCount();
        PrivateFieldAccessHelper.set(getIndexStatisticsResult, "documentCount", _documentCount);

        long _storageSize = obj.getStorageSize();
        PrivateFieldAccessHelper.set(getIndexStatisticsResult, "storageSize", _storageSize);
        return getIndexStatisticsResult;
    }
}
