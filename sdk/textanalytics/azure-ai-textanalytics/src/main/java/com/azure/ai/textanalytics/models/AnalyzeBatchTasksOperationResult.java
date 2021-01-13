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
    private String name;
    private int failedTasksCount;
    private int inProgressTaskCount;
    private int successfullyCompletedTasksCount;
    private int totalTasksCount;
    private OffsetDateTime expiresAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    static {
        AnalyzeBatchOperationResultPropertiesHelper.setAccessor(
            new AnalyzeBatchOperationResultPropertiesHelper.AnalyzeTasksOperationResultAccessor() {
                @Override
                public void setOperationId(AnalyzeBatchTasksOperationResult operationResult, String operationId) {
                    operationResult.setOperationId(operationId);
                }

                @Override
                public void setName(AnalyzeBatchTasksOperationResult operationResult, String name) {
                    operationResult.setName(name);
                }

                @Override
                public void setFailedTasksCount(AnalyzeBatchTasksOperationResult operationResult,
                    int failedTasksCount) {
                    operationResult.setFailedTasksCount(failedTasksCount);
                }

                @Override
                public void setInProgressTaskCount(AnalyzeBatchTasksOperationResult operationResult,
                    int inProgressTaskCount) {
                    operationResult.setInProgressTaskCount(inProgressTaskCount);
                }

                @Override
                public void setSuccessfullyCompletedTasksCount(AnalyzeBatchTasksOperationResult operationResult,
                    int successfullyCompletedTasksCount) {
                    operationResult.setSuccessfullyCompletedTasksCount(successfullyCompletedTasksCount);
                }

                @Override
                public void setTotalTasksCount(AnalyzeBatchTasksOperationResult operationResult, int totalTasksCount) {
                    operationResult.setTotalTasksCount(totalTasksCount);
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
                public void setUpdatedAt(AnalyzeBatchTasksOperationResult operationResult,
                    OffsetDateTime updatedAt) {
                    operationResult.setUpdatedAt(updatedAt);
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
    public String getName() {
        return name;
    }

    /**
     * Gets the failed number of tasks.
     *
     * @return the failed number of tasks.
     */
    public int getFailedTasksCount() {
        return failedTasksCount;
    }

    /**
     * Gets the in-progress number of tasks.
     *
     * @return the in-progress number of tasks.
     */
    public int getInProgressTaskCount() {
        return inProgressTaskCount;
    }

    /**
     * Gets the successfully completed number of tasks.
     *
     * @return the successfully completed number of tasks.
     */
    public int getSuccessfullyCompletedTasksCount() {
        return successfullyCompletedTasksCount;
    }

    /**
     * Gets the total number of tasks.
     *
     * @return the total number of tasks.
     */
    public int getTotalTasksCount() {
        return totalTasksCount;
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
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    private void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    private void setName(String name) {
        this.name = name;
    }

    private void setFailedTasksCount(int failedTasksCount) {
        this.failedTasksCount = failedTasksCount;
    }

    private void setInProgressTaskCount(int inProgressTaskCount) {
        this.inProgressTaskCount = inProgressTaskCount;
    }

    private void setSuccessfullyCompletedTasksCount(int successfullyCompletedTasksCount) {
        this.successfullyCompletedTasksCount = successfullyCompletedTasksCount;
    }

    private void setTotalTasksCount(int totalTasksCount) {
        this.totalTasksCount = totalTasksCount;
    }

    private void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    private void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    private void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
