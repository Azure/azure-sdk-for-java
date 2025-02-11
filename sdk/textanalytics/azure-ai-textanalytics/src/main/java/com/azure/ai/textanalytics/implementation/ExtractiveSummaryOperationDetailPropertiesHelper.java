// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ExtractiveSummaryOperationDetail;

import java.time.OffsetDateTime;

/**
 * The helper class to set the non-public properties of an {@link ExtractiveSummaryOperationDetail} instance.
 */
public final class ExtractiveSummaryOperationDetailPropertiesHelper {
    private static ExtractiveSummaryOperationDetailAccessor accessor;

    private ExtractiveSummaryOperationDetailPropertiesHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link ExtractiveSummaryOperationDetail}
     * instance.
     */
    public interface ExtractiveSummaryOperationDetailAccessor {
        void setOperationId(ExtractiveSummaryOperationDetail operationDetail, String operationId);

        void setDisplayName(ExtractiveSummaryOperationDetail operationDetail, String name);

        void setCreatedAt(ExtractiveSummaryOperationDetail operationDetail, OffsetDateTime createdAt);

        void setExpiresAt(ExtractiveSummaryOperationDetail operationDetail, OffsetDateTime expiresAt);

        void setLastModifiedAt(ExtractiveSummaryOperationDetail operationDetail, OffsetDateTime lastModifiedAt);
    }

    /**
     * The method called from {@link ExtractiveSummaryOperationDetail} to set it's accessor.
     *
     * @param extractiveSummaryOperationDetailAccessor The accessor.
     */
    public static void
        setAccessor(final ExtractiveSummaryOperationDetailAccessor extractiveSummaryOperationDetailAccessor) {
        accessor = extractiveSummaryOperationDetailAccessor;
    }

    public static void setOperationId(ExtractiveSummaryOperationDetail operationDetail, String operationId) {
        accessor.setOperationId(operationDetail, operationId);
    }

    public static void setDisplayName(ExtractiveSummaryOperationDetail operationDetail, String name) {
        accessor.setDisplayName(operationDetail, name);
    }

    public static void setCreatedAt(ExtractiveSummaryOperationDetail operationDetail, OffsetDateTime createdAt) {
        accessor.setCreatedAt(operationDetail, createdAt);
    }

    public static void setExpiresAt(ExtractiveSummaryOperationDetail operationDetail, OffsetDateTime expiresAt) {
        accessor.setExpiresAt(operationDetail, expiresAt);
    }

    public static void setLastModifiedAt(ExtractiveSummaryOperationDetail operationDetail,
        OffsetDateTime lastModifiedAt) {
        accessor.setLastModifiedAt(operationDetail, lastModifiedAt);
    }
}
