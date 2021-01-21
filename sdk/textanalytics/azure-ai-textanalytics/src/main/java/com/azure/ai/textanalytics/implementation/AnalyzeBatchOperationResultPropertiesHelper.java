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
        void setDisplayName(AnalyzeBatchTasksOperationResult operationResult, String name);
        void setFailedTasks(AnalyzeBatchTasksOperationResult operationResult, int failedTasks);
        void setInProgressTasks(AnalyzeBatchTasksOperationResult operationResult, int inProgressTasks);
        void setSuccessfullyCompletedTasks(AnalyzeBatchTasksOperationResult operationResult,
            int successfullyCompletedTasks);
        void setTotalTasks(AnalyzeBatchTasksOperationResult operationResult, int totalTasks);
        void setExpiresAt(AnalyzeBatchTasksOperationResult operationResult, OffsetDateTime expiresAt);
        void setCreatedAt(AnalyzeBatchTasksOperationResult operationResult, OffsetDateTime createdAt);
        void setLastModifiedAt(AnalyzeBatchTasksOperationResult operationResult, OffsetDateTime lastModifiedAt);
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
        accessor.setDisplayName(operationResult, displayName);
    }

    public static void setFailedTasks(AnalyzeBatchTasksOperationResult operationResult, int failedTasks) {
        accessor.setFailedTasks(operationResult, failedTasks);
    }

    public static void setInProgressTasks(AnalyzeBatchTasksOperationResult operationResult,
        int inProgressTasks) {
        accessor.setInProgressTasks(operationResult, inProgressTasks);
    }

    public static void setSuccessfullyCompletedTasks(AnalyzeBatchTasksOperationResult operationResult,
        int successfullyCompletedTasks) {
        accessor.setSuccessfullyCompletedTasks(operationResult, successfullyCompletedTasks);
    }

    public static void setTotalTasks(AnalyzeBatchTasksOperationResult operationResult, int totalTasks) {
        accessor.setTotalTasks(operationResult, totalTasks);
    }

    public static void setCreatedAt(AnalyzeBatchTasksOperationResult operationResult, OffsetDateTime createdAt) {
        accessor.setCreatedAt(operationResult, createdAt);
    }

    public static void setExpiresAt(AnalyzeBatchTasksOperationResult operationResult, OffsetDateTime expiresAt) {
        accessor.setExpiresAt(operationResult, expiresAt);
    }

    public static void setLastModifiedAt(AnalyzeBatchTasksOperationResult operationResult,
        OffsetDateTime lastModifiedAt) {
        accessor.setLastModifiedAt(operationResult, lastModifiedAt);
    }
}
