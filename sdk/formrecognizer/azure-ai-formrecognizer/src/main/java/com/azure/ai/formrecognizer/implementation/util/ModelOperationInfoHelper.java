// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.administration.models.ModelOperationInfo;
import com.azure.ai.formrecognizer.administration.models.ModelOperationKind;
import com.azure.ai.formrecognizer.administration.models.ModelOperationStatus;

import java.time.OffsetDateTime;

/**
 * The helper class to set the non-public properties of an {@link ModelOperationInfo} instance.
 */
public final class ModelOperationInfoHelper {
    private static ModelOperationInfoAccessor accessor;

    private ModelOperationInfoHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link ModelOperationInfo} instance.
     */
    public interface ModelOperationInfoAccessor {

        void setOperationId(ModelOperationInfo modelOperationInfo, String operationId);

        void setStatus(ModelOperationInfo modelOperationInfo, ModelOperationStatus status);

        void setPercentCompleted(ModelOperationInfo modelOperationInfo, Integer percentCompleted);

        void setCreatedOn(ModelOperationInfo modelOperationInfo, OffsetDateTime createdOn);

        void setLastUpdatedOn(ModelOperationInfo modelOperationInfo, OffsetDateTime lastUpdatedOn);

        void setKind(ModelOperationInfo modelOperationInfo, ModelOperationKind kind);

        void setResourceLocation(ModelOperationInfo modelOperationInfo, String resourceLocation);
    }

    /**
     * The method called from {@link ModelOperationInfo} to set it's accessor.
     *
     * @param modelOperationInfoAccessor The accessor.
     */
    public static void setAccessor(
        final ModelOperationInfoHelper.ModelOperationInfoAccessor modelOperationInfoAccessor) {
        accessor = modelOperationInfoAccessor;
    }

    static void setOperationId(ModelOperationInfo modelOperationInfo, String operationId) {
        accessor.setOperationId(modelOperationInfo, operationId);
    }

    static void setStatus(ModelOperationInfo modelOperationInfo, ModelOperationStatus status) {
        accessor.setStatus(modelOperationInfo, status);
    }

    static void setPercentCompleted(ModelOperationInfo modelOperationInfo, Integer percentCompleted) {
        accessor.setPercentCompleted(modelOperationInfo, percentCompleted);
    }

    static void setCreatedOn(ModelOperationInfo modelOperationInfo, OffsetDateTime createdOn) {
        accessor.setCreatedOn(modelOperationInfo, createdOn);
    }

    static void setLastUpdatedOn(ModelOperationInfo modelOperationInfo, OffsetDateTime lastUpdatedOn) {
        accessor.setLastUpdatedOn(modelOperationInfo, lastUpdatedOn);
    }

    static void setKind(ModelOperationInfo modelOperationInfo, ModelOperationKind kind) {
        accessor.setKind(modelOperationInfo, kind);
    }

    static void setResourceLocation(ModelOperationInfo modelOperationInfo, String resourceLocation) {
        accessor.setResourceLocation(modelOperationInfo, resourceLocation);
    }
}
