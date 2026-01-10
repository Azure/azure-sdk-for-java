// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.contentunderstanding.implementation;

import com.azure.ai.contentunderstanding.models.ContentAnalyzerAnalyzeOperationStatus;

/**
 * Helper class to access private members of ContentAnalyzerAnalyzeOperationStatus.
 */
public final class ContentAnalyzerAnalyzeOperationStatusHelper {
    private static ContentAnalyzerAnalyzeOperationStatusAccessor accessor;

    /**
     * Interface for accessing private members.
     */
    public interface ContentAnalyzerAnalyzeOperationStatusAccessor {
        void setOperationId(ContentAnalyzerAnalyzeOperationStatus status, String operationId);
    }

    /**
     * Sets the accessor.
     * 
     * @param accessorInstance the accessor instance.
     */
    public static void setAccessor(ContentAnalyzerAnalyzeOperationStatusAccessor accessorInstance) {
        accessor = accessorInstance;
    }

    /**
     * Sets the operationId on a ContentAnalyzerAnalyzeOperationStatus instance.
     * 
     * @param status the status instance.
     * @param operationId the operationId to set.
     */
    public static void setOperationId(ContentAnalyzerAnalyzeOperationStatus status, String operationId) {
        accessor.setOperationId(status, operationId);
    }

    private ContentAnalyzerAnalyzeOperationStatusHelper() {
    }
}
