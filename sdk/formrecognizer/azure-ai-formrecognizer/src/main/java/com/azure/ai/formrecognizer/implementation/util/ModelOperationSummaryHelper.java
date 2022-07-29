// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.administration.models.ModelOperationSummary;
import com.azure.ai.formrecognizer.administration.models.ModelOperationKind;
import com.azure.ai.formrecognizer.administration.models.ModelOperationStatus;

import java.time.OffsetDateTime;

/**
 * The helper class to set the non-public properties of an {@link ModelOperationSummary} instance.
 */
public final class ModelOperationSummaryHelper {
    private static ModelOperationSummaryAccessor accessor;

    private ModelOperationSummaryHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link ModelOperationSummary} instance.
     */
    public interface ModelOperationSummaryAccessor {

        void setOperationId(ModelOperationSummary modelOperationSummary, String operationId);

        void setStatus(ModelOperationSummary modelOperationSummary, ModelOperationStatus status);

        void setPercentCompleted(ModelOperationSummary modelOperationSummary, Integer percentCompleted);

        void setCreatedOn(ModelOperationSummary modelOperationSummary, OffsetDateTime createdOn);

        void setLastUpdatedOn(ModelOperationSummary modelOperationSummary, OffsetDateTime lastUpdatedOn);

        void setKind(ModelOperationSummary modelOperationSummary, ModelOperationKind kind);

        void setResourceLocation(ModelOperationSummary modelOperationSummary, String resourceLocation);
    }

    /**
     * The method called from {@link ModelOperationSummary} to set it's accessor.
     *
     * @param modelOperationSummaryAccessor The accessor.
     */
    public static void setAccessor(
        final ModelOperationSummaryAccessor modelOperationSummaryAccessor) {
        accessor = modelOperationSummaryAccessor;
    }

    static void setOperationId(ModelOperationSummary modelOperationSummary, String operationId) {
        accessor.setOperationId(modelOperationSummary, operationId);
    }

    static void setStatus(ModelOperationSummary modelOperationSummary, ModelOperationStatus status) {
        accessor.setStatus(modelOperationSummary, status);
    }

    static void setPercentCompleted(ModelOperationSummary modelOperationSummary, Integer percentCompleted) {
        accessor.setPercentCompleted(modelOperationSummary, percentCompleted);
    }

    static void setCreatedOn(ModelOperationSummary modelOperationSummary, OffsetDateTime createdOn) {
        accessor.setCreatedOn(modelOperationSummary, createdOn);
    }

    static void setLastUpdatedOn(ModelOperationSummary modelOperationSummary, OffsetDateTime lastUpdatedOn) {
        accessor.setLastUpdatedOn(modelOperationSummary, lastUpdatedOn);
    }

    static void setKind(ModelOperationSummary modelOperationSummary, ModelOperationKind kind) {
        accessor.setKind(modelOperationSummary, kind);
    }

    static void setResourceLocation(ModelOperationSummary modelOperationSummary, String resourceLocation) {
        accessor.setResourceLocation(modelOperationSummary, resourceLocation);
    }
}
