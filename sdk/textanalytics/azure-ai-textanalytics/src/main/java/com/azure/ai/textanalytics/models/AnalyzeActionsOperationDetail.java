// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.AnalyzeActionsOperationDetailPropertiesHelper;
import com.azure.core.annotation.Immutable;

import java.time.OffsetDateTime;

/**
 * The {@link AnalyzeActionsOperationDetail} model.
 */
@Immutable
public final class AnalyzeActionsOperationDetail {
    private String operationId;
    private String displayName;
    private int failedCount;
    private int inProgressCount;
    private int succeededCount;
    private int totalCount;
    private OffsetDateTime expiresAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastModifiedAt;

    static {
        AnalyzeActionsOperationDetailPropertiesHelper.setAccessor(
            new AnalyzeActionsOperationDetailPropertiesHelper.AnalyzeActionsOperationDetailAccessor() {
                @Override
                public void setOperationId(AnalyzeActionsOperationDetail operationDetail, String operationId) {
                    operationDetail.setOperationId(operationId);
                }

                @Override
                public void setDisplayName(AnalyzeActionsOperationDetail operationDetail, String name) {
                    operationDetail.setDisplayName(name);
                }

                @Override
                public void setFailedCount(AnalyzeActionsOperationDetail operationDetail, int failedCount) {
                    operationDetail.setFailedCount(failedCount);
                }

                @Override
                public void setInProgressCount(AnalyzeActionsOperationDetail operationDetail, int inProgressCount) {
                    operationDetail.setInProgressCount(inProgressCount);
                }

                @Override
                public void setSucceededCount(AnalyzeActionsOperationDetail operationDetail, int succeededCount) {
                    operationDetail.setSucceededCount(succeededCount);
                }

                @Override
                public void setTotalCount(AnalyzeActionsOperationDetail operationDetail, int totalCount) {
                    operationDetail.setTotalCount(totalCount);
                }

                @Override
                public void setExpiresAt(AnalyzeActionsOperationDetail operationDetail,
                    OffsetDateTime expiresAt) {
                    operationDetail.setExpiresAt(expiresAt);
                }

                @Override
                public void setCreatedAt(AnalyzeActionsOperationDetail operationDetail,
                    OffsetDateTime createdAt) {
                    operationDetail.setCreatedAt(createdAt);
                }

                @Override
                public void setLastModifiedAt(AnalyzeActionsOperationDetail operationDetail,
                    OffsetDateTime lastModifiedAt) {
                    operationDetail.setLastModifiedAt(lastModifiedAt);
                }
            });
    }

    /**
     * Gets the operationId property of the {@link AnalyzeActionsOperationDetail}.
     *
     * @return The operationId property of the {@link AnalyzeActionsOperationDetail}.
     */
    public String getOperationId() {
        return operationId;
    }

    /**
     * Gets the displayName property of the {@link AnalyzeActionsOperationDetail}.
     *
     * @return The displayName property of the {@link AnalyzeActionsOperationDetail}.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the failed number of actions.
     *
     * @return The failed number of actions.
     */
    public int getFailedCount() {
        return failedCount;
    }

    /**
     * Gets the in-progress number of actions.
     *
     * @return The in-progress number of actions.
     */
    public int getInProgressCount() {
        return inProgressCount;
    }

    /**
     * Gets the successfully completed number of actions.
     *
     * @return The successfully completed number of actions.
     */
    public int getSucceededCount() {
        return succeededCount;
    }

    /**
     * Gets the total number of actions.
     *
     * @return The total number of actions.
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * Gets the created time of an action.
     *
     * @return The created time of an action.
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the expiration time of an action.
     *
     * @return The expiration time of an action.
     */
    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * Gets the last updated time of an action.
     *
     * @return The last updated time of an action.
     */
    public OffsetDateTime getLastModifiedAt() {
        return lastModifiedAt;
    }

    private void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    private void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    private void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    private void setInProgressCount(int inProgressCount) {
        this.inProgressCount = inProgressCount;
    }

    private void setSucceededCount(int succeededCount) {
        this.succeededCount = succeededCount;
    }

    private void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    private void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    private void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    private void setLastModifiedAt(OffsetDateTime lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }
}
