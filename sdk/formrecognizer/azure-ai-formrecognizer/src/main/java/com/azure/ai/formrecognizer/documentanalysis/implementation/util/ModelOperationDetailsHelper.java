// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.implementation.util;

import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentTypeDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ModelOperationDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ModelOperationKind;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ModelOperationStatus;
import com.azure.core.models.ResponseError;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The helper class to set the non-public properties of an {@link ModelOperationDetails} instance.
 */
public final class ModelOperationDetailsHelper {
    private static ModelOperationDetailsAccessor accessor;

    private ModelOperationDetailsHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link ModelOperationDetails} instance.
     */
    public interface ModelOperationDetailsAccessor {

        void setModelId(ModelOperationDetails modelOperationDetails, String modelId);

        void setDescription(ModelOperationDetails modelOperationDetails, String description);

        void setCreatedOn(ModelOperationDetails modelOperationDetails, OffsetDateTime createdOn);

        void setDocTypes(ModelOperationDetails modelOperationDetails, Map<String, DocumentTypeDetails> docTypes);

        void setError(ModelOperationDetails modelOperationDetails, ResponseError error);

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
     * @param modelOperationDetailsAccessor The accessor.
     */
    public static void setAccessor(final ModelOperationDetailsAccessor modelOperationDetailsAccessor) {
        accessor = modelOperationDetailsAccessor;
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

    static void setDocTypes(ModelOperationDetails modelOperationDetails, Map<String, DocumentTypeDetails> docTypes) {
        accessor.setDocTypes(modelOperationDetails, docTypes);
    }

    static void setError(ModelOperationDetails modelOperationDetails, ResponseError responseError) {
        accessor.setError(modelOperationDetails, responseError);
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
