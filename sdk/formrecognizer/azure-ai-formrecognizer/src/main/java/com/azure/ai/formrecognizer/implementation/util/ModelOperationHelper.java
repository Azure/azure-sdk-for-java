// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.administration.models.DocTypeInfo;
import com.azure.ai.formrecognizer.administration.models.DocumentModelOperationError;
import com.azure.ai.formrecognizer.administration.models.ModelOperationDetails;
import com.azure.ai.formrecognizer.administration.models.ModelOperationKind;
import com.azure.ai.formrecognizer.administration.models.ModelOperationStatus;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The helper class to set the non-public properties of an {@link ModelOperationDetails} instance.
 */
public final class ModelOperationHelper {
    private static ModelOperationAccessor accessor;

    private ModelOperationHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link ModelOperationDetails} instance.
     */
    public interface ModelOperationAccessor {

        void setModelId(ModelOperationDetails modelOperationDetails, String modelId);

        void setDescription(ModelOperationDetails modelOperationDetails, String description);

        void setCreatedOn(ModelOperationDetails modelOperationDetails, OffsetDateTime createdOn);

        void setDocTypes(ModelOperationDetails modelOperationDetails, Map<String, DocTypeInfo> docTypes);

        void setError(ModelOperationDetails modelOperationDetails, DocumentModelOperationError error);

        void setOperationId(ModelOperationDetails modelOperationDetails, String operationId);

        void setStatus(ModelOperationDetails modelOperationDetails, ModelOperationStatus status);

        void setPercentCompleted(ModelOperationDetails modelOperationDetails, Integer percentCompleted);

        void setLastUpdatedOn(ModelOperationDetails modelOperationDetails, OffsetDateTime lastUpdatedOn);

        void setKind(ModelOperationDetails modelOperationDetails, ModelOperationKind kind);

        void setResourceLocation(ModelOperationDetails modelOperationDetails, String resourceLocation);
    }

    /**
     * The method called from {@link ModelOperationDetails} to set it's accessor.
     *
     * @param modelOperationAccessor The accessor.
     */
    public static void setAccessor(final ModelOperationHelper.ModelOperationAccessor modelOperationAccessor) {
        accessor = modelOperationAccessor;
    }

    static void setModelId(ModelOperationDetails modelOperationDetails, String modelId) {
        accessor.setModelId(modelOperationDetails, modelId);
    }

    static void setDescription(ModelOperationDetails modelOperationDetails, String description) {
        accessor.setDescription(modelOperationDetails, description);
    }

    static void setCreatedOn(ModelOperationDetails modelOperationDetails, OffsetDateTime createdOn) {
        accessor.setCreatedOn(modelOperationDetails, createdOn);
    }

    static void setDocTypes(ModelOperationDetails modelOperationDetails, Map<String, DocTypeInfo> docTypes) {
        accessor.setDocTypes(modelOperationDetails, docTypes);
    }

    static void setError(ModelOperationDetails modelOperationDetails, DocumentModelOperationError documentModelOperationError) {
        accessor.setError(modelOperationDetails, documentModelOperationError);
    }

    static void setOperationId(ModelOperationDetails modelOperationDetails, String operationId) {
        accessor.setOperationId(modelOperationDetails, operationId);
    }

    static void setStatus(ModelOperationDetails modelOperationDetails, ModelOperationStatus status) {
        accessor.setStatus(modelOperationDetails, status);
    }

    static void setPercentCompleted(ModelOperationDetails modelOperationDetails, Integer percentCompleted) {
        accessor.setPercentCompleted(modelOperationDetails, percentCompleted);
    }

    static void setLastUpdatedOn(ModelOperationDetails modelOperationDetails, OffsetDateTime lastUpdatedOn) {
        accessor.setLastUpdatedOn(modelOperationDetails, lastUpdatedOn);
    }

    static void setKind(ModelOperationDetails modelOperationDetails, ModelOperationKind kind) {
        accessor.setKind(modelOperationDetails, kind);
    }

    static void setResourceLocation(ModelOperationDetails modelOperationDetails, String resourceLocation) {
        accessor.setResourceLocation(modelOperationDetails, resourceLocation);
    }
}
