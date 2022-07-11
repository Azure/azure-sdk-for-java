// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesOperationDetail;
import com.azure.ai.textanalytics.models.RecognizeCustomEntitiesOperationDetail;

import java.time.OffsetDateTime;

public final class RecognizeCustomEntitiesOperationDetailPropertiesHelper {
    private static RecognizeCustomEntitiesOperationDetailAccessor accessor;

    private RecognizeCustomEntitiesOperationDetailPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an
     * {@link AnalyzeHealthcareEntitiesOperationDetail}
     * instance.
     */
    public interface RecognizeCustomEntitiesOperationDetailAccessor {
        void setOperationId(RecognizeCustomEntitiesOperationDetail operationDetail, String operationId);
        void setCreatedAt(RecognizeCustomEntitiesOperationDetail operationDetail, OffsetDateTime createdAt);
        void setExpiresAt(RecognizeCustomEntitiesOperationDetail operationDetail, OffsetDateTime expiresAt);
        void setLastModifiedAt(RecognizeCustomEntitiesOperationDetail operationDetail,
            OffsetDateTime lastModifiedAt);
    }

    /**
     * The method called from {@link RecognizeCustomEntitiesOperationDetail} to set it's accessor.
     *
     * @param recognizeCustomEntitiesOperationDetailAccessor The accessor.
     */
    public static void setAccessor(
        final RecognizeCustomEntitiesOperationDetailAccessor recognizeCustomEntitiesOperationDetailAccessor) {
        accessor = recognizeCustomEntitiesOperationDetailAccessor;
    }

    public static void setOperationId(RecognizeCustomEntitiesOperationDetail operationDetail, String operationId) {
        accessor.setOperationId(operationDetail, operationId);
    }

    public static void setCreatedAt(RecognizeCustomEntitiesOperationDetail operationDetail,
        OffsetDateTime createdAt) {
        accessor.setCreatedAt(operationDetail, createdAt);
    }

    public static void setExpiresAt(RecognizeCustomEntitiesOperationDetail operationDetail,
        OffsetDateTime expiresAt) {
        accessor.setExpiresAt(operationDetail, expiresAt);
    }

    public static void setLastModifiedAt(RecognizeCustomEntitiesOperationDetail operationDetail,
        OffsetDateTime lastModifiedAt) {
        accessor.setLastModifiedAt(operationDetail, lastModifiedAt);
    }
}
