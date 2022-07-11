// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ClassifyDocumentOperationDetail;

import java.time.OffsetDateTime;

public final class ClassifyDocumentOperationDetailPropertiesHelper {
    private static ClassifyDocumentOperationDetailAccessor accessor;

    private ClassifyDocumentOperationDetailPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an
     * {@link ClassifyDocumentOperationDetailAccessor}
     * instance.
     */
    public interface ClassifyDocumentOperationDetailAccessor {
        void setOperationId(ClassifyDocumentOperationDetail operationDetail, String operationId);
        void setCreatedAt(ClassifyDocumentOperationDetail operationDetail, OffsetDateTime createdAt);
        void setExpiresAt(ClassifyDocumentOperationDetail operationDetail, OffsetDateTime expiresAt);
        void setLastModifiedAt(ClassifyDocumentOperationDetail operationDetail,
            OffsetDateTime lastModifiedAt);
    }

    /**
     * The method called from {@link ClassifyDocumentOperationDetail} to set it's accessor.
     *
     * @param classifyDocumentOperationDetailAccessor The accessor.
     */
    public static void setAccessor(
        final ClassifyDocumentOperationDetailAccessor classifyDocumentOperationDetailAccessor) {
        accessor = classifyDocumentOperationDetailAccessor;
    }

    public static void setOperationId(ClassifyDocumentOperationDetail operationDetail, String operationId) {
        accessor.setOperationId(operationDetail, operationId);
    }

    public static void setCreatedAt(ClassifyDocumentOperationDetail operationDetail,
        OffsetDateTime createdAt) {
        accessor.setCreatedAt(operationDetail, createdAt);
    }

    public static void setExpiresAt(ClassifyDocumentOperationDetail operationDetail,
        OffsetDateTime expiresAt) {
        accessor.setExpiresAt(operationDetail, expiresAt);
    }

    public static void setLastModifiedAt(ClassifyDocumentOperationDetail operationDetail,
        OffsetDateTime lastModifiedAt) {
        accessor.setLastModifiedAt(operationDetail, lastModifiedAt);
    }
}
