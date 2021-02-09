// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AnalyzeBatchActionsOperationDetail;

import java.time.OffsetDateTime;

/**
 * The helper class to set the non-public properties of an {@link AnalyzeBatchActionsOperationDetail} instance.
 */
public final class AnalyzeBatchActionsOperationDetailPropertiesHelper {
    private static AnalyzeBatchActionsOperationDetailAccessor accessor;

    private AnalyzeBatchActionsOperationDetailPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AnalyzeBatchActionsOperationDetail}
     * instance.
     */
    public interface AnalyzeBatchActionsOperationDetailAccessor {
        void setOperationId(AnalyzeBatchActionsOperationDetail operationDetail, String operationId);
        void setDisplayName(AnalyzeBatchActionsOperationDetail operationDetail, String name);
        void setActionsFailed(AnalyzeBatchActionsOperationDetail operationDetail, int actionsFailed);
        void setActionsInProgress(AnalyzeBatchActionsOperationDetail operationDetail, int actionsInProgress);
        void setActionsSucceeded(AnalyzeBatchActionsOperationDetail operationDetail, int actionsSucceeded);
        void setActionsInTotal(AnalyzeBatchActionsOperationDetail operationDetail, int actionsInTotal);
        void setExpiresAt(AnalyzeBatchActionsOperationDetail operationDetail, OffsetDateTime expiresAt);
        void setCreatedAt(AnalyzeBatchActionsOperationDetail operationDetail, OffsetDateTime createdAt);
        void setLastModifiedAt(AnalyzeBatchActionsOperationDetail operationDetail, OffsetDateTime lastModifiedAt);
    }

    /**
     * The method called from {@link AnalyzeBatchActionsOperationDetail} to set it's accessor.
     *
     * @param analyzeTaskOperationResultAccessor The accessor.
     */
    public static void setAccessor(
        final AnalyzeBatchActionsOperationDetailAccessor analyzeTaskOperationResultAccessor) {
        accessor = analyzeTaskOperationResultAccessor;
    }

    public static void setOperationId(AnalyzeBatchActionsOperationDetail operationResult, String operationId) {
        accessor.setOperationId(operationResult, operationId);
    }

    public static void setDisplayName(AnalyzeBatchActionsOperationDetail operationResult, String displayName) {
        accessor.setDisplayName(operationResult, displayName);
    }

    public static void setActionsFailed(AnalyzeBatchActionsOperationDetail operationDetail, int actionsFailed) {
        accessor.setActionsFailed(operationDetail, actionsFailed);
    }

    public static void setActionsInProgress(AnalyzeBatchActionsOperationDetail operationDetail, int actionsInProgress) {
        accessor.setActionsInProgress(operationDetail, actionsInProgress);
    }

    public static void setActionsSucceeded(AnalyzeBatchActionsOperationDetail operationDetail, int actionsSucceeded) {
        accessor.setActionsSucceeded(operationDetail, actionsSucceeded);
    }

    public static void setActionsInTotal(AnalyzeBatchActionsOperationDetail operationDetail, int actionsInTotal) {
        accessor.setActionsInTotal(operationDetail, actionsInTotal);
    }

    public static void setCreatedAt(AnalyzeBatchActionsOperationDetail operationDetail, OffsetDateTime createdAt) {
        accessor.setCreatedAt(operationDetail, createdAt);
    }

    public static void setExpiresAt(AnalyzeBatchActionsOperationDetail operationDetail, OffsetDateTime expiresAt) {
        accessor.setExpiresAt(operationDetail, expiresAt);
    }

    public static void setLastModifiedAt(AnalyzeBatchActionsOperationDetail operationDetail,
        OffsetDateTime lastModifiedAt) {
        accessor.setLastModifiedAt(operationDetail, lastModifiedAt);
    }
}
