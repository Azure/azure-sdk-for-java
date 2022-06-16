// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeMultiCategoryClassifyOperationDetail;

import java.time.OffsetDateTime;

public final class AnalyzeMultiCategoryClassifyOperationDetailPropertiesHelper {
    private static AnalyzeMultiCategoryClassifyOperationDetailAccessor accessor;

    private AnalyzeMultiCategoryClassifyOperationDetailPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an
     * {@link AnalyzeHealthcareEntitiesOperationDetail}
     * instance.
     */
    public interface AnalyzeMultiCategoryClassifyOperationDetailAccessor {
        void setOperationId(AnalyzeMultiCategoryClassifyOperationDetail operationDetail, String operationId);
        void setCreatedAt(AnalyzeMultiCategoryClassifyOperationDetail operationDetail, OffsetDateTime createdAt);
        void setExpiresAt(AnalyzeMultiCategoryClassifyOperationDetail operationDetail, OffsetDateTime expiresAt);
        void setLastModifiedAt(AnalyzeMultiCategoryClassifyOperationDetail operationDetail,
            OffsetDateTime lastModifiedAt);
    }

    /**
     * The method called from {@link AnalyzeMultiCategoryClassifyOperationDetail} to set it's accessor.
     *
     * @param analyzeMultiCategoryClassifyOperationDetailAccessor The accessor.
     */
    public static void setAccessor(
        final AnalyzeMultiCategoryClassifyOperationDetailAccessor analyzeMultiCategoryClassifyOperationDetailAccessor) {
        accessor = analyzeMultiCategoryClassifyOperationDetailAccessor;
    }

    public static void setOperationId(AnalyzeMultiCategoryClassifyOperationDetail operationDetail, String operationId) {
        accessor.setOperationId(operationDetail, operationId);
    }

    public static void setCreatedAt(AnalyzeMultiCategoryClassifyOperationDetail operationDetail,
        OffsetDateTime createdAt) {
        accessor.setCreatedAt(operationDetail, createdAt);
    }

    public static void setExpiresAt(AnalyzeMultiCategoryClassifyOperationDetail operationDetail,
        OffsetDateTime expiresAt) {
        accessor.setExpiresAt(operationDetail, expiresAt);
    }

    public static void setLastModifiedAt(AnalyzeMultiCategoryClassifyOperationDetail operationDetail,
        OffsetDateTime lastModifiedAt) {
        accessor.setLastModifiedAt(operationDetail, lastModifiedAt);
    }
}
