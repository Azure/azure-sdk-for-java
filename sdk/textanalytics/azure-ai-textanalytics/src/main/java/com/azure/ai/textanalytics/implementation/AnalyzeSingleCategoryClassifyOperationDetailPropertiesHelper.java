// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeCategoryClassifyOperationDetail;

import java.time.OffsetDateTime;

public final class AnalyzeSingleCategoryClassifyOperationDetailPropertiesHelper {
    private static AnalyzeSingleCategoryClassifyOperationDetailAccessor accessor;

    private AnalyzeSingleCategoryClassifyOperationDetailPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an
     * {@link AnalyzeHealthcareEntitiesOperationDetail}
     * instance.
     */
    public interface AnalyzeSingleCategoryClassifyOperationDetailAccessor {
        void setOperationId(AnalyzeCategoryClassifyOperationDetail operationDetail, String operationId);
        void setCreatedAt(AnalyzeCategoryClassifyOperationDetail operationDetail, OffsetDateTime createdAt);
        void setExpiresAt(AnalyzeCategoryClassifyOperationDetail operationDetail, OffsetDateTime expiresAt);
        void setLastModifiedAt(AnalyzeCategoryClassifyOperationDetail operationDetail,
            OffsetDateTime lastModifiedAt);
    }

    /**
     * The method called from {@link AnalyzeCategoryClassifyOperationDetail} to set it's accessor.
     *
     * @param AnalyzeSingleCategoryClassifyOperationDetailAccessor The accessor.
     */
    public static void setAccessor(
        final AnalyzeSingleCategoryClassifyOperationDetailAccessor AnalyzeSingleCategoryClassifyOperationDetailAccessor) {
        accessor = AnalyzeSingleCategoryClassifyOperationDetailAccessor;
    }

    public static void setOperationId(AnalyzeCategoryClassifyOperationDetail operationDetail, String operationId) {
        accessor.setOperationId(operationDetail, operationId);
    }

    public static void setCreatedAt(AnalyzeCategoryClassifyOperationDetail operationDetail,
        OffsetDateTime createdAt) {
        accessor.setCreatedAt(operationDetail, createdAt);
    }

    public static void setExpiresAt(AnalyzeCategoryClassifyOperationDetail operationDetail,
        OffsetDateTime expiresAt) {
        accessor.setExpiresAt(operationDetail, expiresAt);
    }

    public static void setLastModifiedAt(AnalyzeCategoryClassifyOperationDetail operationDetail,
        OffsetDateTime lastModifiedAt) {
        accessor.setLastModifiedAt(operationDetail, lastModifiedAt);
    }
}
