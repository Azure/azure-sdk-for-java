// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.LabelClassifyOperationDetailPropertiesHelper;
import com.azure.core.annotation.Immutable;

import java.time.OffsetDateTime;

/**
 * The {@link LabelClassifyOperationDetail} model.
 */
@Immutable
public final class LabelClassifyOperationDetail {
    private String operationId;
    private OffsetDateTime createdAt;
    private OffsetDateTime expiresAt;
    private OffsetDateTime lastModifiedAt;

    static {
        LabelClassifyOperationDetailPropertiesHelper.setAccessor(
            new LabelClassifyOperationDetailPropertiesHelper.LabelClassifyOperationDetailAccessor() {
                @Override
                public void setOperationId(LabelClassifyOperationDetail operationResult,
                    String operationId) {
                    operationResult.setOperationId(operationId);
                }

                @Override
                public void setExpiresAt(LabelClassifyOperationDetail operationDetail,
                    OffsetDateTime expiresAt) {
                    operationDetail.setExpiresAt(expiresAt);
                }

                @Override
                public void setCreatedAt(LabelClassifyOperationDetail operationDetail,
                    OffsetDateTime createdAt) {
                    operationDetail.setCreatedAt(createdAt);
                }

                @Override
                public void setLastModifiedAt(LabelClassifyOperationDetail operationDetail,
                    OffsetDateTime lastModifiedAt) {
                    operationDetail.setLastModifiedAt(lastModifiedAt);
                }
            }
        );
    }

    /**
     * Gets the operationId property of the {@link LabelClassifyOperationDetail}.
     *
     * @return The operationId property of the {@link LabelClassifyOperationDetail}.
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
