// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.models.DocumentOperationResult;

/**
 * The helper class to set the non-public properties of an {@link DocumentOperationResult} instance.
 */
public final class DocumentOperationResultHelper {
    private static DocumentOperationResultAccessor accessor;

    private DocumentOperationResultHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentOperationResult} instance.
     */
    public interface DocumentOperationResultAccessor {
        void setResultId(DocumentOperationResult documentOperationResult, String resultId);
    }

    /**
     * The method called from {@link DocumentOperationResult} to set it's accessor.
     *
     * @param documentOperationResultAccessor The accessor.
     */
    public static void setAccessor(final DocumentOperationResultAccessor documentOperationResultAccessor) {
        accessor = documentOperationResultAccessor;
    }

    public static void setResultId(DocumentOperationResult documentOperationResult, String resultId) {
        accessor.setResultId(documentOperationResult, resultId);
    }
}
