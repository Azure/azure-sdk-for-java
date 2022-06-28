// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOperationDetail;
import com.azure.ai.textanalytics.models.LabelClassifyOperationDetail;

import java.time.OffsetDateTime;

public final class LabelClassifyOperationDetailPropertiesHelper {
    private static LabelClassifyOperationDetailAccessor accessor;

    private LabelClassifyOperationDetailPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an
     * {@link AnalyzeHealthcareEntitiesOperationDetail}
     * instance.
     */
    public interface LabelClassifyOperationDetailAccessor {
        void setOperationId(LabelClassifyOperationDetail operationDetail, String operationId);
        void setCreatedAt(LabelClassifyOperationDetail operationDetail, OffsetDateTime createdAt);
        void setExpiresAt(LabelClassifyOperationDetail operationDetail, OffsetDateTime expiresAt);
        void setLastModifiedAt(LabelClassifyOperationDetail operationDetail,
            OffsetDateTime lastModifiedAt);
    }

    /**
     * The method called from {@link LabelClassifyOperationDetail} to set it's accessor.
     *
     * @param labelClassifyOperationDetailAccessor The accessor.
     */
    public static void setAccessor(
        final LabelClassifyOperationDetailAccessor labelClassifyOperationDetailAccessor) {
        accessor = labelClassifyOperationDetailAccessor;
    }

    public static void setOperationId(LabelClassifyOperationDetail operationDetail, String operationId) {
        accessor.setOperationId(operationDetail, operationId);
    }

    public static void setCreatedAt(LabelClassifyOperationDetail operationDetail,
        OffsetDateTime createdAt) {
        accessor.setCreatedAt(operationDetail, createdAt);
    }

    public static void setExpiresAt(LabelClassifyOperationDetail operationDetail,
        OffsetDateTime expiresAt) {
        accessor.setExpiresAt(operationDetail, expiresAt);
    }

    public static void setLastModifiedAt(LabelClassifyOperationDetail operationDetail,
        OffsetDateTime lastModifiedAt) {
        accessor.setLastModifiedAt(operationDetail, lastModifiedAt);
    }
}
