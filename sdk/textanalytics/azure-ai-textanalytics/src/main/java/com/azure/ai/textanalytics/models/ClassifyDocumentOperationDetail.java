// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.ClassifyDocumentOperationDetailPropertiesHelper;
import com.azure.core.annotation.Immutable;

import java.time.OffsetDateTime;

/**
 * The {@link ClassifyDocumentOperationDetail} model.
 */
@Immutable
public final class ClassifyDocumentOperationDetail {
    private String operationId;
    private String displayName;
    private OffsetDateTime createdAt;
    private OffsetDateTime expiresAt;
    private OffsetDateTime lastModifiedAt;

    static {
        ClassifyDocumentOperationDetailPropertiesHelper.setAccessor(
            new ClassifyDocumentOperationDetailPropertiesHelper.ClassifyDocumentOperationDetailAccessor() {
                @Override
                public void setOperationId(ClassifyDocumentOperationDetail operationResult,
                    String operationId) {
                    operationResult.setOperationId(operationId);
                }

                @Override
                public void setDisplayName(ClassifyDocumentOperationDetail operationDetail, String name) {
                    operationDetail.setDisplayName(name);
                }

                @Override
                public void setExpiresAt(ClassifyDocumentOperationDetail operationDetail,
                    OffsetDateTime expiresAt) {
                    operationDetail.setExpiresAt(expiresAt);
                }

                @Override
                public void setCreatedAt(ClassifyDocumentOperationDetail operationDetail,
                    OffsetDateTime createdAt) {
                    operationDetail.setCreatedAt(createdAt);
                }

                @Override
                public void setLastModifiedAt(ClassifyDocumentOperationDetail operationDetail,
                    OffsetDateTime lastModifiedAt) {
                    operationDetail.setLastModifiedAt(lastModifiedAt);
                }
            }
        );
    }

    /**
     * Gets the operationId property of the {@link ClassifyDocumentOperationDetail}.
     *
     * @return The operationId property of the {@link ClassifyDocumentOperationDetail}.
     */
    public String getOperationId() {
        return operationId;
    }

    /**
     * Gets the displayName property of the {@link ClassifyDocumentOperationDetail}.
     *
     * @return The displayName property of the {@link ClassifyDocumentOperationDetail}.
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
