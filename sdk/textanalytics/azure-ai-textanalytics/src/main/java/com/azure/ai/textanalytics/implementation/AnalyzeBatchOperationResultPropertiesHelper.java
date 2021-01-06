// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AnalyzeBatchOperationResult;
import com.azure.ai.textanalytics.models.AnalyzeBatchResult;

import java.time.OffsetDateTime;

public final class AnalyzeBatchOperationResultPropertiesHelper {

    private static AnalyzeBatchOperationResultPropertiesHelper.AnalyzeTasksOperationResultAccessor accessor;

    private AnalyzeBatchOperationResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AnalyzeBatchOperationResult}
     * instance.
     */
    public interface AnalyzeTasksOperationResultAccessor {
        void setOperationId(AnalyzeBatchOperationResult operationResult, String operationId);
        void setDisplayName(AnalyzeBatchOperationResult operationResult, String displayName);
        void setFailedTasksCount(AnalyzeBatchOperationResult operationResult, int failedTasksCount);
        void setInProgressTaskCount(AnalyzeBatchOperationResult operationResult, int inProgressTaskCount);
        void setSuccessfullyCompletedTasksCount(AnalyzeBatchOperationResult operationResult,
            int successfullyCompletedTasksCount);
        void setTotalTasksCount(AnalyzeBatchOperationResult operationResult, int totalTasksCount);
        void setExpiresAt(AnalyzeBatchOperationResult operationResult, OffsetDateTime expiresAt);
        void setCreatedAt(AnalyzeBatchOperationResult operationResult, OffsetDateTime createdAt);
        void setUpdatedAt(AnalyzeBatchOperationResult operationResult, OffsetDateTime updatedAt);

        void setAnalyzeBatchResult(AnalyzeBatchOperationResult operationResult, AnalyzeBatchResult analyzeBatchResult);
    }

    /**
     * The method called from {@link AnalyzeBatchOperationResult} to set it's accessor.
     *
     * @param analyzeTaskOperationResultAccessor The accessor.
     */
    public static void setAccessor(
        final AnalyzeBatchOperationResultPropertiesHelper.AnalyzeTasksOperationResultAccessor
            analyzeTaskOperationResultAccessor) {
        accessor = analyzeTaskOperationResultAccessor;
    }

    public static void setOperationId(AnalyzeBatchOperationResult operationResult, String operationId) {
        accessor.setOperationId(operationResult, operationId);
    }

    public static void setDisplayName(AnalyzeBatchOperationResult operationResult, String displayName) {
        accessor.setDisplayName(operationResult, displayName);
    }

    public static void setFailedTasksCount(AnalyzeBatchOperationResult operationResult, int failedTasksCount) {
        accessor.setFailedTasksCount(operationResult, failedTasksCount);
    }

    public static void setInProgressTaskCount(AnalyzeBatchOperationResult operationResult,
        int inProgressTaskCount) {
        accessor.setInProgressTaskCount(operationResult, inProgressTaskCount);
    }

    public static void setSuccessfullyCompletedTasksCount(AnalyzeBatchOperationResult operationResult,
        int successfullyCompletedTasksCount) {
        accessor.setSuccessfullyCompletedTasksCount(operationResult, successfullyCompletedTasksCount);
    }

    public static void setTotalTasksCount(AnalyzeBatchOperationResult operationResult, int totalTasksCount) {
        accessor.setTotalTasksCount(operationResult, totalTasksCount);
    }

    public static void setCreatedAt(AnalyzeBatchOperationResult operationResult, OffsetDateTime createdAt) {
        accessor.setCreatedAt(operationResult, createdAt);
    }

    public static void setExpiresAt(AnalyzeBatchOperationResult operationResult, OffsetDateTime expiresAt) {
        accessor.setExpiresAt(operationResult, expiresAt);
    }

    public static void setUpdatedAt(AnalyzeBatchOperationResult operationResult, OffsetDateTime updatedAt) {
        accessor.setUpdatedAt(operationResult, updatedAt);
    }

    public static void setAnalyzeBatchResult(AnalyzeBatchOperationResult operationResult, AnalyzeBatchResult analyzeBatchResult) {
        accessor.setAnalyzeBatchResult(operationResult, analyzeBatchResult);
    }
}
