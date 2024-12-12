// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence.implementation;

import com.azure.ai.documentintelligence.models.AnalyzeBatchOperation;

/**
 * The helper class to set the non-public properties of an {@link AnalyzeBatchOperation} instance.
 */
public class AnalyzeBatchOperationHelper {

    private static AnalyzeBatchOperationAccessor accessor;

    private AnalyzeBatchOperationHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link AnalyzeBatchOperation} instance.
     */
    public interface AnalyzeBatchOperationAccessor {
        void setOperationId(AnalyzeBatchOperation analyzeBatchOperation, String operationId);
    }

    /**
     * The method called from {@link AnalyzeBatchOperation} to set it's accessor.
     *
     * @param analyzeBatchOperationAccessor The accessor.
     */
    public static void setAccessor(final AnalyzeBatchOperationAccessor analyzeBatchOperationAccessor) {
        accessor = analyzeBatchOperationAccessor;
    }

    public static void setOperationId(AnalyzeBatchOperation analyzeBatchOperation, String operationId) {
        accessor.setOperationId(analyzeBatchOperation, operationId);
    }
}
