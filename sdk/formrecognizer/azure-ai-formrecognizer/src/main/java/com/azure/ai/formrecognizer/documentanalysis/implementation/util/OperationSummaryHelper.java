// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.implementation.util;

import com.azure.ai.formrecognizer.documentanalysis.administration.models.OperationStatus;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.OperationSummary;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.OperationKind;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The helper class to set the non-public properties of an {@link OperationSummary} instance.
 */
public final class OperationSummaryHelper {
    private static OperationSummaryAccessor accessor;

    private OperationSummaryHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link OperationSummary} instance.
     */
    public interface OperationSummaryAccessor {

        void setOperationId(OperationSummary operationSummary, String operationId);

        void setStatus(OperationSummary operationSummary, OperationStatus status);

        void setPercentCompleted(OperationSummary operationSummary, Integer percentCompleted);

        void setCreatedOn(OperationSummary operationSummary, OffsetDateTime createdOn);

        void setLastUpdatedOn(OperationSummary operationSummary, OffsetDateTime lastUpdatedOn);

        void setKind(OperationSummary operationSummary, OperationKind kind);

        void setResourceLocation(OperationSummary operationSummary, String resourceLocation);
        void setTags(OperationSummary operationSummary, Map<String, String> tags);
    }

    /**
     * The method called from {@link OperationSummary} to set it's accessor.
     *
     * @param operationSummaryAccessor The accessor.
     */
    public static void setAccessor(
        final OperationSummaryAccessor operationSummaryAccessor) {
        accessor = operationSummaryAccessor;
    }

    static void setOperationId(OperationSummary operationSummary, String operationId) {
        accessor.setOperationId(operationSummary, operationId);
    }

    static void setStatus(OperationSummary operationSummary, OperationStatus status) {
        accessor.setStatus(operationSummary, status);
    }

    static void setPercentCompleted(OperationSummary operationSummary, Integer percentCompleted) {
        accessor.setPercentCompleted(operationSummary, percentCompleted);
    }

    static void setCreatedOn(OperationSummary operationSummary, OffsetDateTime createdOn) {
        accessor.setCreatedOn(operationSummary, createdOn);
    }

    static void setLastUpdatedOn(OperationSummary operationSummary, OffsetDateTime lastUpdatedOn) {
        accessor.setLastUpdatedOn(operationSummary, lastUpdatedOn);
    }

    static void setKind(OperationSummary operationSummary, OperationKind kind) {
        accessor.setKind(operationSummary, kind);
    }

    static void setResourceLocation(OperationSummary operationSummary, String resourceLocation) {
        accessor.setResourceLocation(operationSummary, resourceLocation);
    }

    static void setTags(OperationSummary operationSummary, Map<String, String> tags) {
        accessor.setTags(operationSummary, tags);
    }
}
