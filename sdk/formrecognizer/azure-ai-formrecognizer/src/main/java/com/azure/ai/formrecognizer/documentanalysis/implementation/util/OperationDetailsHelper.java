// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.implementation.util;

import com.azure.ai.formrecognizer.documentanalysis.administration.models.OperationKind;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.OperationStatus;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.OperationDetails;
import com.azure.core.models.ResponseError;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The helper class to set the non-public properties of an {@link OperationDetails} instance.
 */
public final class OperationDetailsHelper {
    private static OperationDetailsAccessor accessor;

    private OperationDetailsHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link OperationDetails} instance.
     */
    public interface OperationDetailsAccessor {

        void setCreatedOn(OperationDetails operationDetails, OffsetDateTime createdOn);

        void setError(OperationDetails operationDetails, ResponseError error);

        void setOperationId(OperationDetails operationDetails, String operationId);

        void setStatus(OperationDetails operationDetails, OperationStatus status);

        void setPercentCompleted(OperationDetails operationDetails, Integer percentCompleted);

        void setLastUpdatedOn(OperationDetails operationDetails, OffsetDateTime lastUpdatedOn);

        void setKind(OperationDetails operationDetails, OperationKind kind);

        void setResourceLocation(OperationDetails operationDetails, String resourceLocation);
        void setTags(OperationDetails operationDetails, Map<String, String> tags);
    }

    /**
     * The method called from {@link OperationDetails} to set it's accessor.
     *
     * @param modelOperationDetailsAccessor The accessor.
     */
    public static void setAccessor(final OperationDetailsAccessor modelOperationDetailsAccessor) {
        accessor = modelOperationDetailsAccessor;
    }

    static void setCreatedOn(OperationDetails operationDetails, OffsetDateTime createdOn) {
        accessor.setCreatedOn(operationDetails, createdOn);
    }

    static void setError(OperationDetails operationDetails, ResponseError responseError) {
        accessor.setError(operationDetails, responseError);
    }

    static void setOperationId(OperationDetails operationDetails, String operationId) {
        accessor.setOperationId(operationDetails, operationId);
    }

    static void setStatus(OperationDetails operationDetails, OperationStatus status) {
        accessor.setStatus(operationDetails, status);
    }

    static void setPercentCompleted(OperationDetails operationDetails, Integer percentCompleted) {
        accessor.setPercentCompleted(operationDetails, percentCompleted);
    }

    static void setLastUpdatedOn(OperationDetails operationDetails, OffsetDateTime lastUpdatedOn) {
        accessor.setLastUpdatedOn(operationDetails, lastUpdatedOn);
    }

    static void setKind(OperationDetails operationDetails, OperationKind kind) {
        accessor.setKind(operationDetails, kind);
    }

    static void setResourceLocation(OperationDetails operationDetails, String resourceLocation) {
        accessor.setResourceLocation(operationDetails, resourceLocation);
    }
    static void setTags(OperationDetails operationDetails, Map<String, String> tags) {
        accessor.setTags(operationDetails, tags);
    }
}
