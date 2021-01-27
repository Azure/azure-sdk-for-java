// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper;

import java.time.OffsetDateTime;

/**
 * The {@link AnalyzeHealthcareEntitiesOperationDetail} model.
 */
public final class AnalyzeHealthcareEntitiesOperationDetail {
    private String operationId;
    private OffsetDateTime createdAt;
    private OffsetDateTime expiresAt;
    private OffsetDateTime lastModifiedAt;

    static {
        AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper.setAccessor(
            new AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper
                    .AnalyzeHealthcareEntitiesOperationDetailAccessor() {
                @Override
                public void setOperationId(AnalyzeHealthcareEntitiesOperationDetail operationResult,
                    String operationId) {
                    operationResult.setOperationId(operationId);
                }

                @Override
                public void setExpiresAt(AnalyzeHealthcareEntitiesOperationDetail operationDetail,
                    OffsetDateTime expiresAt) {
                    operationDetail.setExpiresAt(expiresAt);
                }

                @Override
                public void setCreatedAt(AnalyzeHealthcareEntitiesOperationDetail operationDetail,
                    OffsetDateTime createdAt) {
                    operationDetail.setCreatedAt(createdAt);
                }

                @Override
                public void setLastModifiedAt(AnalyzeHealthcareEntitiesOperationDetail operationDetail,
                    OffsetDateTime lastModifiedAt) {
                    operationDetail.setLastModifiedAt(lastModifiedAt);
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
