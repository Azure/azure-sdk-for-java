// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.AnalyzeBatchOperationResultPropertiesHelper;

import java.time.OffsetDateTime;

/**
 * The {@link AnalyzeBatchTasksOperationResult} model.
 */
public final class AnalyzeBatchTasksOperationResult {
    private String operationId;
    private String displayName;
    private int failedTasks;
    private int inProgressTasks;
    private int successfullyCompletedTasks;
    private int totalTasks;
    private OffsetDateTime expiresAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastModifiedAt;

    static {
        AnalyzeBatchOperationResultPropertiesHelper.setAccessor(
            new AnalyzeBatchOperationResultPropertiesHelper.AnalyzeTasksOperationResultAccessor() {
                @Override
                public void setOperationId(AnalyzeBatchTasksOperationResult operationResult, String operationId) {
                    operationResult.setOperationId(operationId);
                }

                @Override
                public void setDisplayName(AnalyzeBatchTasksOperationResult operationResult, String name) {
                    operationResult.setDisplayName(name);
                }

                @Override
                public void setFailedTasks(AnalyzeBatchTasksOperationResult operationResult,
                    int failedTasks) {
                    operationResult.setFailedTasks(failedTasks);
                }

                @Override
                public void setInProgressTasks(AnalyzeBatchTasksOperationResult operationResult,
                    int inProgressTasks) {
                    operationResult.setInProgressTasks(inProgressTasks);
                }

                @Override
                public void setSuccessfullyCompletedTasks(AnalyzeBatchTasksOperationResult operationResult,
                    int successfullyCompletedTasks) {
                    operationResult.setSuccessfullyCompletedTasks(successfullyCompletedTasks);
                }

                @Override
                public void setTotalTasks(AnalyzeBatchTasksOperationResult operationResult, int totalTasks) {
                    operationResult.setTotalTasks(totalTasks);
                }

                @Override
                public void setExpiresAt(AnalyzeBatchTasksOperationResult operationResult,
                    OffsetDateTime expiresAt) {
                    operationResult.setExpiresAt(expiresAt);
                }

                @Override
                public void setCreatedAt(AnalyzeBatchTasksOperationResult operationResult,
                    OffsetDateTime createdAt) {
                    operationResult.setCreatedAt(createdAt);
                }

                @Override
                public void setLastModifiedAt(AnalyzeBatchTasksOperationResult operationResult,
                    OffsetDateTime lastModifiedAt) {
                    operationResult.setLastModifiedAt(lastModifiedAt);
                }
            });
    }

    /**
     * Gets the operationId property of the {@link AnalyzeBatchTasksOperationResult}.
     *
     * @return the operationId property of the {@link AnalyzeBatchTasksOperationResult}.
     */
    public String getOperationId() {
        return operationId;
    }

    /**
     * Gets the displayName property of the {@link AnalyzeBatchTasksOperationResult}.
     *
     * @return the displayName property of the {@link AnalyzeBatchTasksOperationResult}.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the failed number of tasks.
     *
     * @return the failed number of tasks.
     */
    public int getFailedTasks() {
        return failedTasks;
    }

    /**
     * Gets the in-progress number of tasks.
     *
     * @return the in-progress number of tasks.
     */
    public int getInProgressTasks() {
        return inProgressTasks;
    }

    /**
     * Gets the successfully completed number of tasks.
     *
     * @return the successfully completed number of tasks.
     */
    public int getSuccessfullyCompletedTasks() {
        return successfullyCompletedTasks;
    }

    /**
     * Gets the total number of tasks.
     *
     * @return the total number of tasks.
     */
    public int getTotalTasks() {
        return totalTasks;
    }

    /**
     * Gets the created time of a task.
     *
     * @return the created time of a task.
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the expiration time of a task.
     *
     * @return the expiration time of a task.
     */
    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * Gets the last updated time of a task.
     *
     * @return the last updated time of a task.
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

    private void setFailedTasks(int failedTasks) {
        this.failedTasks = failedTasks;
    }

    private void setInProgressTasks(int inProgressTasks) {
        this.inProgressTasks = inProgressTasks;
    }

    private void setSuccessfullyCompletedTasks(int successfullyCompletedTasks) {
        this.successfullyCompletedTasks = successfullyCompletedTasks;
    }

    private void setTotalTasks(int totalTasks) {
        this.totalTasks = totalTasks;
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
