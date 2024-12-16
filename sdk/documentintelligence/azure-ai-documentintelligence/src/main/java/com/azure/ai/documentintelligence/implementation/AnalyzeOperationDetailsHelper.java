// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence.implementation;

import com.azure.ai.documentintelligence.models.AnalyzeOperationDetails;

/**
 * The helper class to set the non-public properties of an {@link AnalyzeOperationDetails} instance.
 */
public final class AnalyzeOperationDetailsHelper {

    private static AnalyzeOperationDetailsAccessor accessor;

    private AnalyzeOperationDetailsHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link AnalyzeOperationDetails} instance.
     */
    public interface AnalyzeOperationDetailsAccessor {
        /**
         * Sets the operationId property of an {@link AnalyzeOperationDetails} instance.
         * @param analyzeOperation The AnalyzeOperationDetails instance.
         * @param operationId The operationId value to set.
         */
        void setOperationId(AnalyzeOperationDetails analyzeOperation, String operationId);
    }

    /**
     * The method called from {@link AnalyzeOperationDetails} to set it's accessor.
     *
     * @param analyzeOperationAccessor The accessor.
     */
    public static void setAccessor(final AnalyzeOperationDetailsAccessor analyzeOperationAccessor) {
        accessor = analyzeOperationAccessor;
    }

    /**
     * Sets the operationId property of an {@link AnalyzeOperationDetails} instance.
     * @param analyzeOperation The AnalyzeOperationDetails instance.
     * @param operationId The operationId value to set.
     */
    public static void setOperationId(AnalyzeOperationDetails analyzeOperation, String operationId) {
        accessor.setOperationId(analyzeOperation, operationId);
    }
}
