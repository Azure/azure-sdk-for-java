// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ExtractSummaryOperationDetail;

import java.time.OffsetDateTime;

/**
 * The helper class to set the non-public properties of an {@link ExtractSummaryOperationDetail} instance.
 */
public final class ExtractSummaryOperationDetailPropertiesHelper {
    private static ExtractSummaryOperationDetailAccessor accessor;

    private ExtractSummaryOperationDetailPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ExtractSummaryOperationDetail}
     * instance.
     */
    public interface ExtractSummaryOperationDetailAccessor {
        void setOperationId(ExtractSummaryOperationDetail operationDetail, String operationId);
        void setDisplayName(ExtractSummaryOperationDetail operationDetail, String name);
        void setCreatedAt(ExtractSummaryOperationDetail operationDetail, OffsetDateTime createdAt);
        void setExpiresAt(ExtractSummaryOperationDetail operationDetail, OffsetDateTime expiresAt);
        void setLastModifiedAt(ExtractSummaryOperationDetail operationDetail, OffsetDateTime lastModifiedAt);
    }

    /**
     * The method called from {@link ExtractSummaryOperationDetail} to set it's accessor.
     *
     * @param extractSummaryOperationDetailAccessor The accessor.
     */
    public static void setAccessor(
        final ExtractSummaryOperationDetailAccessor extractSummaryOperationDetailAccessor) {
        accessor = extractSummaryOperationDetailAccessor;
    }

    public static void setOperationId(ExtractSummaryOperationDetail operationDetail, String operationId) {
        accessor.setOperationId(operationDetail, operationId);
    }

    public static void setDisplayName(ExtractSummaryOperationDetail operationDetail, String name) {
        accessor.setDisplayName(operationDetail, name);
    }

    public static void setCreatedAt(ExtractSummaryOperationDetail operationDetail, OffsetDateTime createdAt) {
        accessor.setCreatedAt(operationDetail, createdAt);
    }

    public static void setExpiresAt(ExtractSummaryOperationDetail operationDetail, OffsetDateTime expiresAt) {
        accessor.setExpiresAt(operationDetail, expiresAt);
    }

    public static void setLastModifiedAt(ExtractSummaryOperationDetail operationDetail, OffsetDateTime lastModifiedAt) {
        accessor.setLastModifiedAt(operationDetail, lastModifiedAt);
    }
}
