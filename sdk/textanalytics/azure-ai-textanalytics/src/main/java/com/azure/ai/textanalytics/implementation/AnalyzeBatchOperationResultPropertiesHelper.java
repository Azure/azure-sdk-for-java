// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AnalyzeBatchTasksOperationResult;

import java.time.OffsetDateTime;

public final class AnalyzeBatchOperationResultPropertiesHelper {

    private static AnalyzeBatchOperationResultPropertiesHelper.AnalyzeTasksOperationResultAccessor accessor;

    private AnalyzeBatchOperationResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AnalyzeBatchTasksOperationResult}
     * instance.
     */
    public interface AnalyzeTasksOperationResultAccessor {
        void setOperationId(AnalyzeBatchTasksOperationResult operationResult, String operationId);
        void setName(AnalyzeBatchTasksOperationResult operationResult, String name);
        void setFailedTasksCount(AnalyzeBatchTasksOperationResult operationResult, int failedTasksCount);
        void setInProgressTaskCount(AnalyzeBatchTasksOperationResult operationResult, int inProgressTaskCount);
        void setSuccessfullyCompletedTasksCount(AnalyzeBatchTasksOperationResult operationResult,
            int successfullyCompletedTasksCount);
        void setTotalTasksCount(AnalyzeBatchTasksOperationResult operationResult, int totalTasksCount);
        void setExpiresAt(AnalyzeBatchTasksOperationResult operationResult, OffsetDateTime expiresAt);
        void setCreatedAt(AnalyzeBatchTasksOperationResult operationResult, OffsetDateTime createdAt);
        void setUpdatedAt(AnalyzeBatchTasksOperationResult operationResult, OffsetDateTime updatedAt);
    }

    /**
     * The method called from {@link AnalyzeBatchTasksOperationResult} to set it's accessor.
     *
     * @param analyzeTaskOperationResultAccessor The accessor.
     */
    public static void setAccessor(
        final AnalyzeBatchOperationResultPropertiesHelper.AnalyzeTasksOperationResultAccessor
            analyzeTaskOperationResultAccessor) {
        accessor = analyzeTaskOperationResultAccessor;
    }

    public static void setOperationId(AnalyzeBatchTasksOperationResult operationResult, String operationId) {
        accessor.setOperationId(operationResult, operationId);
    }

    public static void setDisplayName(AnalyzeBatchTasksOperationResult operationResult, String displayName) {
        accessor.setName(operationResult, displayName);
    }

    public static void setFailedTasksCount(AnalyzeBatchTasksOperationResult operationResult, int failedTasksCount) {
        accessor.setFailedTasksCount(operationResult, failedTasksCount);
    }

    public static void setInProgressTaskCount(AnalyzeBatchTasksOperationResult operationResult,
        int inProgressTaskCount) {
        accessor.setInProgressTaskCount(operationResult, inProgressTaskCount);
    }

    public static void setSuccessfullyCompletedTasksCount(AnalyzeBatchTasksOperationResult operationResult,
        int successfullyCompletedTasksCount) {
        accessor.setSuccessfullyCompletedTasksCount(operationResult, successfullyCompletedTasksCount);
    }

    public static void setTotalTasksCount(AnalyzeBatchTasksOperationResult operationResult, int totalTasksCount) {
        accessor.setTotalTasksCount(operationResult, totalTasksCount);
    }

    public static void setCreatedAt(AnalyzeBatchTasksOperationResult operationResult, OffsetDateTime createdAt) {
        accessor.setCreatedAt(operationResult, createdAt);
    }

    public static void setExpiresAt(AnalyzeBatchTasksOperationResult operationResult, OffsetDateTime expiresAt) {
        accessor.setExpiresAt(operationResult, expiresAt);
    }

    public static void setUpdatedAt(AnalyzeBatchTasksOperationResult operationResult, OffsetDateTime updatedAt) {
        accessor.setUpdatedAt(operationResult, updatedAt);
    }
}
