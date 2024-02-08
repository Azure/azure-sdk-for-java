// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.RecognizeCustomEntitiesOperationDetailPropertiesHelper;
import com.azure.core.annotation.Immutable;

import java.time.OffsetDateTime;

/**
 * The {@code RecognizeCustomEntitiesOperationDetail} model.
 */
@Immutable
public final class RecognizeCustomEntitiesOperationDetail {
    private String operationId;
    private String displayName;
    private OffsetDateTime createdAt;
    private OffsetDateTime expiresAt;
    private OffsetDateTime lastModifiedAt;

    static {
        RecognizeCustomEntitiesOperationDetailPropertiesHelper.setAccessor(
            new RecognizeCustomEntitiesOperationDetailPropertiesHelper.
                    RecognizeCustomEntitiesOperationDetailAccessor() {
                @Override
                public void setOperationId(RecognizeCustomEntitiesOperationDetail operationResult,
                    String operationId) {
                    operationResult.setOperationId(operationId);
                }

                @Override
                public void setDisplayName(RecognizeCustomEntitiesOperationDetail operationDetail, String name) {
                    operationDetail.setDisplayName(name);
                }

                @Override
                public void setExpiresAt(RecognizeCustomEntitiesOperationDetail operationDetail,
                    OffsetDateTime expiresAt) {
                    operationDetail.setExpiresAt(expiresAt);
                }

                @Override
                public void setCreatedAt(RecognizeCustomEntitiesOperationDetail operationDetail,
                    OffsetDateTime createdAt) {
                    operationDetail.setCreatedAt(createdAt);
                }

                @Override
                public void setLastModifiedAt(RecognizeCustomEntitiesOperationDetail operationDetail,
                    OffsetDateTime lastModifiedAt) {
                    operationDetail.setLastModifiedAt(lastModifiedAt);
                }
            }
        );
    }

    /**
     * Constructs a {@code RecognizeCustomEntitiesOperationDetail} model.
     */
    public RecognizeCustomEntitiesOperationDetail() {
    }

    /**
     * Gets the operationId property of the {@code RecognizeCustomEntitiesOperationDetail}.
     *
     * @return The operationId property of the {@code RecognizeCustomEntitiesOperationDetail}.
     */
    public String getOperationId() {
        return operationId;
    }

    /**
     * Gets the displayName property of the {@code RecognizeCustomEntitiesOperationDetail}.
     *
     * @return The displayName property of the {@code RecognizeCustomEntitiesOperationDetail}.
     */
    public String getDisplayName() {
        return displayName;
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
