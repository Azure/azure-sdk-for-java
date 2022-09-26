// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.implementation.util;

import com.azure.ai.formrecognizer.documentanalysis.models.OperationResult;

/**
 * The helper class to set the non-public properties of an {@link OperationResult} instance.
 */
public final class OperationResultHelper {
    private static OperationResultAccessor accessor;

    private OperationResultHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link OperationResult} instance.
     */
    public interface OperationResultAccessor {
        void setOperationId(OperationResult operationResult, String resultId);
    }

    /**
     * The method called from {@link OperationResult} to set it's accessor.
     *
     * @param operationResultAccessor The accessor.
     */
    public static void setAccessor(final OperationResultAccessor operationResultAccessor) {
        accessor = operationResultAccessor;
    }

    public static void setResultId(OperationResult operationResult, String operationId) {
        accessor.setOperationId(operationResult, operationId);
    }
}
