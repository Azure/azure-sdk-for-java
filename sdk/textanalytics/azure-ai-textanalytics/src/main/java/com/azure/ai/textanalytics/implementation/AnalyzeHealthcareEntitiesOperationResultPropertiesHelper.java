// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOperationResult;

import java.time.OffsetDateTime;

public final class AnalyzeHealthcareEntitiesOperationResultPropertiesHelper {
    private static AnalyzeHealthcareEntitiesOperationResultAccessor accessor;

    private AnalyzeHealthcareEntitiesOperationResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AnalyzeHealthcareEntitiesOperationResult}
     * instance.
     */
    public interface AnalyzeHealthcareEntitiesOperationResultAccessor {
        void setOperationId(AnalyzeHealthcareEntitiesOperationResult operationResult, String operationId);
        void setCreatedAt(AnalyzeHealthcareEntitiesOperationResult operationResult, OffsetDateTime createdAt);
        void setExpiresAt(AnalyzeHealthcareEntitiesOperationResult operationResult, OffsetDateTime expiresAt);
        void setUpdatedAt(AnalyzeHealthcareEntitiesOperationResult operationResult, OffsetDateTime updatedAt);
    }

    /**
     * The method called from {@link AnalyzeHealthcareEntitiesOperationResult} to set it's accessor.
     *
     * @param analyzeHealthcareEntitiesOperationResultAccessor The accessor.
     */
    public static void setAccessor(
        final AnalyzeHealthcareEntitiesOperationResultAccessor analyzeHealthcareEntitiesOperationResultAccessor) {
        accessor = analyzeHealthcareEntitiesOperationResultAccessor;
    }

    public static void setOperationId(AnalyzeHealthcareEntitiesOperationResult operationResult, String operationId) {
        accessor.setOperationId(operationResult, operationId);
    }

    public static void setCreatedAt(AnalyzeHealthcareEntitiesOperationResult operationResult,
        OffsetDateTime createdAt) {
        accessor.setCreatedAt(operationResult, createdAt);
    }

    public static void setExpiresAt(AnalyzeHealthcareEntitiesOperationResult operationResult,
        OffsetDateTime expiresAt) {
        accessor.setExpiresAt(operationResult, expiresAt);
    }

    public static void setUpdatedAt(AnalyzeHealthcareEntitiesOperationResult operationResult,
        OffsetDateTime updatedAt) {
        accessor.setUpdatedAt(operationResult, updatedAt);
    }
}
