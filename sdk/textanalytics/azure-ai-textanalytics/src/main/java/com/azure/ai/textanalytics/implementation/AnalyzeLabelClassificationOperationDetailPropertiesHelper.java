// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOperationDetail;
import com.azure.ai.textanalytics.models.AnalyzeLabelClassificationOperationDetail;

import java.time.OffsetDateTime;

public final class AnalyzeLabelClassificationOperationDetailPropertiesHelper {
    private static AnalyzeLabelClassificationOperationDetailAccessor accessor;

    private AnalyzeLabelClassificationOperationDetailPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an
     * {@link AnalyzeHealthcareEntitiesOperationDetail}
     * instance.
     */
    public interface AnalyzeLabelClassificationOperationDetailAccessor {
        void setOperationId(AnalyzeLabelClassificationOperationDetail operationDetail, String operationId);
        void setCreatedAt(AnalyzeLabelClassificationOperationDetail operationDetail, OffsetDateTime createdAt);
        void setExpiresAt(AnalyzeLabelClassificationOperationDetail operationDetail, OffsetDateTime expiresAt);
        void setLastModifiedAt(AnalyzeLabelClassificationOperationDetail operationDetail,
            OffsetDateTime lastModifiedAt);
    }

    /**
     * The method called from {@link AnalyzeLabelClassificationOperationDetail} to set it's accessor.
     *
     * @param analyzeLabelClassificationOperationDetailAccessor The accessor.
     */
    public static void setAccessor(
        final AnalyzeLabelClassificationOperationDetailAccessor analyzeLabelClassificationOperationDetailAccessor) {
        accessor = analyzeLabelClassificationOperationDetailAccessor;
    }

    public static void setOperationId(AnalyzeLabelClassificationOperationDetail operationDetail, String operationId) {
        accessor.setOperationId(operationDetail, operationId);
    }

    public static void setCreatedAt(AnalyzeLabelClassificationOperationDetail operationDetail,
        OffsetDateTime createdAt) {
        accessor.setCreatedAt(operationDetail, createdAt);
    }

    public static void setExpiresAt(AnalyzeLabelClassificationOperationDetail operationDetail,
        OffsetDateTime expiresAt) {
        accessor.setExpiresAt(operationDetail, expiresAt);
    }

    public static void setLastModifiedAt(AnalyzeLabelClassificationOperationDetail operationDetail,
        OffsetDateTime lastModifiedAt) {
        accessor.setLastModifiedAt(operationDetail, lastModifiedAt);
    }
}
