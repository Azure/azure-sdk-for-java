// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.AnalyzeBatchActionsOperationDetailPropertiesHelper;

import java.time.OffsetDateTime;

/**
 * The {@link AnalyzeBatchActionsOperationDetail} model.
 */
public final class AnalyzeBatchActionsOperationDetail {
    private String operationId;
    private String displayName;
    private int actionsFailed;
    private int actionsInProgress;
    private int actionsSucceeded;
    private int actionsInTotal;
    private OffsetDateTime expiresAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastModifiedAt;

    static {
        AnalyzeBatchActionsOperationDetailPropertiesHelper.setAccessor(
            new AnalyzeBatchActionsOperationDetailPropertiesHelper.AnalyzeBatchActionsOperationDetailAccessor() {
                @Override
                public void setOperationId(AnalyzeBatchActionsOperationDetail operationDetail, String operationId) {
                    operationDetail.setOperationId(operationId);
                }

                @Override
                public void setDisplayName(AnalyzeBatchActionsOperationDetail operationDetail, String name) {
                    operationDetail.setDisplayName(name);
                }

                @Override
                public void setActionsFailed(AnalyzeBatchActionsOperationDetail operationDetail,
                    int actionsFailed) {
                    operationDetail.setActionsFailed(actionsFailed);
                }

                @Override
                public void setActionsInProgress(AnalyzeBatchActionsOperationDetail operationDetail,
                    int actionsInProgress) {
                    operationDetail.setActionsInProgress(actionsInProgress);
                }

                @Override
                public void setActionsSucceeded(AnalyzeBatchActionsOperationDetail operationDetail,
                    int actionsSucceeded) {
                    operationDetail.setActionsSucceeded(actionsSucceeded);
                }

                @Override
                public void setActionsInTotal(AnalyzeBatchActionsOperationDetail operationDetail, int actionsInTotal) {
                    operationDetail.setActionsInTotal(actionsInTotal);
                }

                @Override
                public void setExpiresAt(AnalyzeBatchActionsOperationDetail operationDetail,
                    OffsetDateTime expiresAt) {
                    operationDetail.setExpiresAt(expiresAt);
                }

                @Override
                public void setCreatedAt(AnalyzeBatchActionsOperationDetail operationDetail,
                    OffsetDateTime createdAt) {
                    operationDetail.setCreatedAt(createdAt);
                }

                @Override
                public void setLastModifiedAt(AnalyzeBatchActionsOperationDetail operationDetail,
                    OffsetDateTime lastModifiedAt) {
                    operationDetail.setLastModifiedAt(lastModifiedAt);
                }
            });
    }

    /**
     * Gets the operationId property of the {@link AnalyzeBatchActionsOperationDetail}.
     *
     * @return the operationId property of the {@link AnalyzeBatchActionsOperationDetail}.
     */
    public String getOperationId() {
        return operationId;
    }

    /**
     * Gets the displayName property of the {@link AnalyzeBatchActionsOperationDetail}.
     *
     * @return the displayName property of the {@link AnalyzeBatchActionsOperationDetail}.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the failed number of actions.
     *
     * @return the failed number of actions.
     */
    public int getActionsFailed() {
        return actionsFailed;
    }

    /**
     * Gets the in-progress number of actions.
     *
     * @return the in-progress number of actions.
     */
    public int getActionsInProgress() {
        return actionsInProgress;
    }

    /**
     * Gets the successfully completed number of actions.
     *
     * @return the successfully completed number of actions.
     */
    public int getActionsSucceeded() {
        return actionsSucceeded;
    }

    /**
     * Gets the total number of actions.
     *
     * @return the total number of actions.
     */
    public int getActionsInTotal() {
        return actionsInTotal;
    }

    /**
     * Gets the created time of an action.
     *
     * @return the created time of an action.
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the expiration time of an action.
     *
     * @return the expiration time of an action.
     */
    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * Gets the last updated time of an action.
     *
     * @return the last updated time of an action.
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

    private void setActionsFailed(int actionsFailed) {
        this.actionsFailed = actionsFailed;
    }

    private void setActionsInProgress(int actionsInProgress) {
        this.actionsInProgress = actionsInProgress;
    }

    private void setActionsSucceeded(int actionsSucceeded) {
        this.actionsSucceeded = actionsSucceeded;
    }

    private void setActionsInTotal(int actionsInTotal) {
        this.actionsInTotal = actionsInTotal;
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
