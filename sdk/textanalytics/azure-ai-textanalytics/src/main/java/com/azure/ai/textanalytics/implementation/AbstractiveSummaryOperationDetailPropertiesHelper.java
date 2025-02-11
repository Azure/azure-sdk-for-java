// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AbstractiveSummaryOperationDetail;

import java.time.OffsetDateTime;

/**
 * The helper class to set the non-public properties of an {@link AbstractiveSummaryOperationDetail} instance.
 */
public final class AbstractiveSummaryOperationDetailPropertiesHelper {
    private static AbstractiveSummaryOperationDetailAccessor accessor;

    private AbstractiveSummaryOperationDetailPropertiesHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link AbstractiveSummaryOperationDetail}
     * instance.
     */
    public interface AbstractiveSummaryOperationDetailAccessor {
        void setOperationId(AbstractiveSummaryOperationDetail operationDetail, String operationId);

        void setDisplayName(AbstractiveSummaryOperationDetail operationDetail, String name);

        void setCreatedAt(AbstractiveSummaryOperationDetail operationDetail, OffsetDateTime createdAt);

        void setExpiresAt(AbstractiveSummaryOperationDetail operationDetail, OffsetDateTime expiresAt);

        void setLastModifiedAt(AbstractiveSummaryOperationDetail operationDetail, OffsetDateTime lastModifiedAt);
    }

    /**
     * The method called from {@link AbstractiveSummaryOperationDetail} to set it's accessor.
     *
     * @param abstractiveSummaryOperationDetailAccessor The accessor.
     */
    public static void
        setAccessor(final AbstractiveSummaryOperationDetailAccessor abstractiveSummaryOperationDetailAccessor) {
        accessor = abstractiveSummaryOperationDetailAccessor;
    }

    public static void setOperationId(AbstractiveSummaryOperationDetail operationDetail, String operationId) {
        accessor.setOperationId(operationDetail, operationId);
    }

    public static void setDisplayName(AbstractiveSummaryOperationDetail operationDetail, String name) {
        accessor.setDisplayName(operationDetail, name);
    }

    public static void setCreatedAt(AbstractiveSummaryOperationDetail operationDetail, OffsetDateTime createdAt) {
        accessor.setCreatedAt(operationDetail, createdAt);
    }

    public static void setExpiresAt(AbstractiveSummaryOperationDetail operationDetail, OffsetDateTime expiresAt) {
        accessor.setExpiresAt(operationDetail, expiresAt);
    }

    public static void setLastModifiedAt(AbstractiveSummaryOperationDetail operationDetail,
        OffsetDateTime lastModifiedAt) {
        accessor.setLastModifiedAt(operationDetail, lastModifiedAt);
    }
}
