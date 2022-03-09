// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AnalyzeActionsOperationDetail;

import java.time.OffsetDateTime;

/**
 * The helper class to set the non-public properties of an {@link AnalyzeActionsOperationDetail} instance.
 */
public final class AnalyzeActionsOperationDetailPropertiesHelper {
    private static AnalyzeActionsOperationDetailAccessor accessor;

    private AnalyzeActionsOperationDetailPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AnalyzeActionsOperationDetail}
     * instance.
     */
    public interface AnalyzeActionsOperationDetailAccessor {
        void setOperationId(AnalyzeActionsOperationDetail operationDetail, String operationId);
        void setDisplayName(AnalyzeActionsOperationDetail operationDetail, String name);
        void setFailedCount(AnalyzeActionsOperationDetail operationDetail, int failedCount);
        void setInProgressCount(AnalyzeActionsOperationDetail operationDetail, int inProgressCount);
        void setSucceededCount(AnalyzeActionsOperationDetail operationDetail, int succeededCount);
        void setTotalCount(AnalyzeActionsOperationDetail operationDetail, int totalCount);
        void setExpiresAt(AnalyzeActionsOperationDetail operationDetail, OffsetDateTime expiresAt);
        void setCreatedAt(AnalyzeActionsOperationDetail operationDetail, OffsetDateTime createdAt);
        void setLastModifiedAt(AnalyzeActionsOperationDetail operationDetail, OffsetDateTime lastModifiedAt);
    }

    /**
     * The method called from {@link AnalyzeActionsOperationDetail} to set it's accessor.
     *
     * @param analyzeActionsOperationDetailAccessor The accessor.
     */
    public static void setAccessor(
        final AnalyzeActionsOperationDetailAccessor analyzeActionsOperationDetailAccessor) {
        accessor = analyzeActionsOperationDetailAccessor;
    }

    public static void setOperationId(AnalyzeActionsOperationDetail operationResult, String operationId) {
        accessor.setOperationId(operationResult, operationId);
    }

    public static void setDisplayName(AnalyzeActionsOperationDetail operationResult, String displayName) {
        accessor.setDisplayName(operationResult, displayName);
    }

    public static void setActionsFailed(AnalyzeActionsOperationDetail operationDetail, int actionsFailed) {
        accessor.setFailedCount(operationDetail, actionsFailed);
    }

    public static void setActionsInProgress(AnalyzeActionsOperationDetail operationDetail, int actionsInProgress) {
        accessor.setInProgressCount(operationDetail, actionsInProgress);
    }

    public static void setActionsSucceeded(AnalyzeActionsOperationDetail operationDetail, int actionsSucceeded) {
        accessor.setSucceededCount(operationDetail, actionsSucceeded);
    }

    public static void setActionsInTotal(AnalyzeActionsOperationDetail operationDetail, int actionsInTotal) {
        accessor.setTotalCount(operationDetail, actionsInTotal);
    }

    public static void setCreatedAt(AnalyzeActionsOperationDetail operationDetail, OffsetDateTime createdAt) {
        accessor.setCreatedAt(operationDetail, createdAt);
    }

    public static void setExpiresAt(AnalyzeActionsOperationDetail operationDetail, OffsetDateTime expiresAt) {
        accessor.setExpiresAt(operationDetail, expiresAt);
    }

    public static void setLastModifiedAt(AnalyzeActionsOperationDetail operationDetail, OffsetDateTime lastModifiedAt) {
        accessor.setLastModifiedAt(operationDetail, lastModifiedAt);
    }
}
