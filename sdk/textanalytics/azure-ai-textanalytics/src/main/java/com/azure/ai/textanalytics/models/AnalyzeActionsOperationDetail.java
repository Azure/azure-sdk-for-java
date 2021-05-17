// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.AnalyzeActionsOperationDetailPropertiesHelper;

import java.time.OffsetDateTime;

/**
 * The {@link AnalyzeActionsOperationDetail} model.
 */
public final class AnalyzeActionsOperationDetail {
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
                public void setActionsFailed(AnalyzeActionsOperationDetail operationDetail,
                    int actionsFailed) {
                    operationDetail.setActionsFailed(actionsFailed);
                }

                @Override
                public void setActionsInProgress(AnalyzeActionsOperationDetail operationDetail,
                    int actionsInProgress) {
                    operationDetail.setActionsInProgress(actionsInProgress);
                }

                @Override
                public void setActionsSucceeded(AnalyzeActionsOperationDetail operationDetail,
                    int actionsSucceeded) {
                    operationDetail.setActionsSucceeded(actionsSucceeded);
                }

                @Override
                public void setActionsInTotal(AnalyzeActionsOperationDetail operationDetail, int actionsInTotal) {
                    operationDetail.setActionsInTotal(actionsInTotal);
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
     * @return the operationId property of the {@link AnalyzeActionsOperationDetail}.
     */
    public String getOperationId() {
        return operationId;
    }

    /**
     * Gets the displayName property of the {@link AnalyzeActionsOperationDetail}.
     *
     * @return the displayName property of the {@link AnalyzeActionsOperationDetail}.
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
