// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper;
import com.azure.core.annotation.Immutable;

import java.time.OffsetDateTime;

/**
 * The {@link AnalyzeHealthcareEntitiesOperationDetail} model.
 */
@Immutable
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
     * Gets the operationId property of the {@link AnalyzeHealthcareEntitiesOperationDetail}.
     *
     * @return The operationId property of the {@link AnalyzeHealthcareEntitiesOperationDetail}.
     */
    public String getOperationId() {
        return operationId;
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
