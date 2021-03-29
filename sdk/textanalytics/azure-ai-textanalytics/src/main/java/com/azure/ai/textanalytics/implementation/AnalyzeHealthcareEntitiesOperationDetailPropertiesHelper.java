// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOperationDetail;

import java.time.OffsetDateTime;

public final class AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper {
    private static AnalyzeHealthcareEntitiesOperationDetailAccessor accessor;

    private AnalyzeHealthcareEntitiesOperationDetailPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an
     * {@link AnalyzeHealthcareEntitiesOperationDetail}
     * instance.
     */
    public interface AnalyzeHealthcareEntitiesOperationDetailAccessor {
        void setOperationId(AnalyzeHealthcareEntitiesOperationDetail operationDetail, String operationId);
        void setCreatedAt(AnalyzeHealthcareEntitiesOperationDetail operationDetail, OffsetDateTime createdAt);
        void setExpiresAt(AnalyzeHealthcareEntitiesOperationDetail operationDetail, OffsetDateTime expiresAt);
        void setLastModifiedAt(AnalyzeHealthcareEntitiesOperationDetail operationDetail,
            OffsetDateTime lastModifiedAt);
    }

    /**
     * The method called from {@link AnalyzeHealthcareEntitiesOperationDetail} to set it's accessor.
     *
     * @param analyzeHealthcareEntitiesOperationDetailAccessor The accessor.
     */
    public static void setAccessor(
        final AnalyzeHealthcareEntitiesOperationDetailAccessor analyzeHealthcareEntitiesOperationDetailAccessor) {
        accessor = analyzeHealthcareEntitiesOperationDetailAccessor;
    }

    public static void setOperationId(AnalyzeHealthcareEntitiesOperationDetail operationDetail, String operationId) {
        accessor.setOperationId(operationDetail, operationId);
    }

    public static void setCreatedAt(AnalyzeHealthcareEntitiesOperationDetail operationDetail,
        OffsetDateTime createdAt) {
        accessor.setCreatedAt(operationDetail, createdAt);
    }

    public static void setExpiresAt(AnalyzeHealthcareEntitiesOperationDetail operationDetail,
        OffsetDateTime expiresAt) {
        accessor.setExpiresAt(operationDetail, expiresAt);
    }

    public static void setLastModifiedAt(AnalyzeHealthcareEntitiesOperationDetail operationDetail,
        OffsetDateTime lastModifiedAt) {
        accessor.setLastModifiedAt(operationDetail, lastModifiedAt);
    }
}
