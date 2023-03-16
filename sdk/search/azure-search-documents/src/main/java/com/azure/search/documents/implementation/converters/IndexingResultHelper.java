// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.models.IndexingResult;

/**
 * The helper class to set the non-public properties of an {@link IndexingResult} instance.
 */
public final class IndexingResultHelper {
    private static IndexingResultAccessor accessor;

    private IndexingResultHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link IndexingResult} instance.
     */
    public interface IndexingResultAccessor {
        void setErrorMessage(IndexingResult indexingResult, String errorMessage);
    }

    /**
     * The method called from {@link IndexingResult} to set it's accessor.
     *
     * @param indexingResultAccessor The accessor.
     */
    public static void setAccessor(final IndexingResultAccessor indexingResultAccessor) {
        accessor = indexingResultAccessor;
    }

    static void setErrorMessage(IndexingResult indexingResult, String errorMessage) {
        accessor.setErrorMessage(indexingResult, errorMessage);
    }
}
