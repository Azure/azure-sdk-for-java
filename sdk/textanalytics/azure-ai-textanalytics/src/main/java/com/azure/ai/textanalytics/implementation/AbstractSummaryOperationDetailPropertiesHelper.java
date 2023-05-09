// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AbstractSummaryOperationDetail;

import java.time.OffsetDateTime;

/**
 * The helper class to set the non-public properties of an {@link AbstractSummaryOperationDetail} instance.
 */
public final class AbstractSummaryOperationDetailPropertiesHelper {
    private static AbstractSummaryOperationDetailAccessor accessor;

    private AbstractSummaryOperationDetailPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AbstractSummaryOperationDetail}
     * instance.
     */
    public interface AbstractSummaryOperationDetailAccessor {
        void setOperationId(AbstractSummaryOperationDetail operationDetail, String operationId);
        void setDisplayName(AbstractSummaryOperationDetail operationDetail, String name);
        void setCreatedAt(AbstractSummaryOperationDetail operationDetail, OffsetDateTime createdAt);
        void setExpiresAt(AbstractSummaryOperationDetail operationDetail, OffsetDateTime expiresAt);
        void setLastModifiedAt(AbstractSummaryOperationDetail operationDetail, OffsetDateTime lastModifiedAt);
    }

    /**
     * The method called from {@link AbstractSummaryOperationDetail} to set it's accessor.
     *
     * @param abstractSummaryOperationDetailAccessor The accessor.
     */
    public static void setAccessor(
        final AbstractSummaryOperationDetailAccessor abstractSummaryOperationDetailAccessor) {
        accessor = abstractSummaryOperationDetailAccessor;
    }

    public static void setOperationId(AbstractSummaryOperationDetail operationDetail, String operationId) {
        accessor.setOperationId(operationDetail, operationId);
    }

    public static void setDisplayName(AbstractSummaryOperationDetail operationDetail, String name) {
        accessor.setDisplayName(operationDetail, name);
    }

    public static void setCreatedAt(AbstractSummaryOperationDetail operationDetail, OffsetDateTime createdAt) {
        accessor.setCreatedAt(operationDetail, createdAt);
    }

    public static void setExpiresAt(AbstractSummaryOperationDetail operationDetail, OffsetDateTime expiresAt) {
        accessor.setExpiresAt(operationDetail, expiresAt);
    }

    public static void setLastModifiedAt(AbstractSummaryOperationDetail operationDetail,
        OffsetDateTime lastModifiedAt) {
        accessor.setLastModifiedAt(operationDetail, lastModifiedAt);
    }
}
