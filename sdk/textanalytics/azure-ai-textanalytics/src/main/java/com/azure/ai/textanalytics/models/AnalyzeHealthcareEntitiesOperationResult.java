// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.AnalyzeHealthcareEntitiesOperationResultPropertiesHelper;

import java.time.OffsetDateTime;

/**
 * The {@link AnalyzeHealthcareEntitiesOperationResult} model.
 */
public final class AnalyzeHealthcareEntitiesOperationResult {
    private String operationId;
    private OffsetDateTime createdAt;
    private OffsetDateTime expiresAt;
    private OffsetDateTime lastModifiedAt;

    static {
        AnalyzeHealthcareEntitiesOperationResultPropertiesHelper.setAccessor(
            new AnalyzeHealthcareEntitiesOperationResultPropertiesHelper
                    .AnalyzeHealthcareEntitiesOperationResultAccessor() {
                @Override
                public void setOperationId(AnalyzeHealthcareEntitiesOperationResult operationResult,
                    String operationId) {
                    operationResult.setOperationId(operationId);
                }

                @Override
                public void setExpiresAt(AnalyzeHealthcareEntitiesOperationResult operationResult,
                    OffsetDateTime expiresAt) {
                    operationResult.setExpiresAt(expiresAt);
                }

                @Override
                public void setCreatedAt(AnalyzeHealthcareEntitiesOperationResult operationResult,
                    OffsetDateTime createdAt) {
                    operationResult.setCreatedAt(createdAt);
                }

                @Override
                public void setLastModifiedAt(AnalyzeHealthcareEntitiesOperationResult operationResult,
                    OffsetDateTime lastModifiedAt) {
                    operationResult.setLastModifiedAt(lastModifiedAt);
                }
            }
        );
    }

    /**
     * Gets the taskId property of the HealthcareTaskOperationResult.
     *
     * @return the taskId property of the HealthcareTaskOperationResult.
     */
    public String getOperationId() {
        return operationId;
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
