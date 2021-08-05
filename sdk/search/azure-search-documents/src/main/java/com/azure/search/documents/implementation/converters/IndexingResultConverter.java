// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.models.IndexingResult;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.IndexingResult} and
 * {@link IndexingResult}.
 */
public final class IndexingResultConverter {
    /**
     * Maps from {@link com.azure.search.documents.implementation.models.IndexingResult} to {@link IndexingResult}.
     */
    public static IndexingResult map(com.azure.search.documents.implementation.models.IndexingResult obj) {
        if (obj == null) {
            return null;
        }
        IndexingResult indexingResult = new IndexingResult(obj.getKey(), obj.isSucceeded(), obj.getStatusCode());

        String errorMessage = obj.getErrorMessage();
        IndexingResultHelper.setErrorMessage(indexingResult, errorMessage);

        return indexingResult;
    }

    private IndexingResultConverter() {
    }
}
