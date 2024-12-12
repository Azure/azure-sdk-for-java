// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence.implementation;

import com.azure.ai.documentintelligence.models.AnalyzeOperation;

/**
 * The helper class to set the non-public properties of an {@link AnalyzeOperation} instance.
 */
public final class AnalyzeOperationHelper {

    private static AnalyzeOperationAccessor accessor;

    private AnalyzeOperationHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link AnalyzeOperation} instance.
     */
    public interface AnalyzeOperationAccessor {
        void setOperationId(AnalyzeOperation analyzeOperation, String operationId);
    }

    /**
     * The method called from {@link AnalyzeOperation} to set it's accessor.
     *
     * @param analyzeOperationAccessor The accessor.
     */
    public static void setAccessor(final AnalyzeOperationAccessor analyzeOperationAccessor) {
        accessor = analyzeOperationAccessor;
    }

    public static void setOperationId(AnalyzeOperation analyzeOperation, String operationId) {
        accessor.setOperationId(analyzeOperation, operationId);
    }
}
