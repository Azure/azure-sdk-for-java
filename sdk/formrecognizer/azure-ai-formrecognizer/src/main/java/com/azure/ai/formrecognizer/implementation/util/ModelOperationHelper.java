// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.administration.models.DocTypeInfo;
import com.azure.ai.formrecognizer.administration.models.FormRecognizerError;
import com.azure.ai.formrecognizer.administration.models.ModelOperation;
import com.azure.ai.formrecognizer.administration.models.ModelOperationKind;
import com.azure.ai.formrecognizer.administration.models.ModelOperationStatus;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The helper class to set the non-public properties of an {@link ModelOperation} instance.
 */
public final class ModelOperationHelper {
    private static ModelOperationAccessor accessor;

    private ModelOperationHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link ModelOperation} instance.
     */
    public interface ModelOperationAccessor {

        void setModelId(ModelOperation modelOperation, String modelId);

        void setDescription(ModelOperation modelOperation, String description);

        void setCreatedOn(ModelOperation modelOperation, OffsetDateTime createdOn);

        void setDocTypes(ModelOperation modelOperation, Map<String, DocTypeInfo> docTypes);

        void setError(ModelOperation modelOperation, FormRecognizerError error);

        void setOperationId(ModelOperation modelOperation, String operationId);

        void setStatus(ModelOperation modelOperation, ModelOperationStatus status);

        void setPercentCompleted(ModelOperation modelOperation, Integer percentCompleted);

        void setLastUpdatedOn(ModelOperation modelOperation, OffsetDateTime lastUpdatedOn);

        void setKind(ModelOperation modelOperation, ModelOperationKind kind);

        void setResourceLocation(ModelOperation modelOperation, String resourceLocation);
    }

    /**
     * The method called from {@link ModelOperation} to set it's accessor.
     *
     * @param modelOperationAccessor The accessor.
     */
    public static void setAccessor(final ModelOperationHelper.ModelOperationAccessor modelOperationAccessor) {
        accessor = modelOperationAccessor;
    }

    static void setModelId(ModelOperation modelOperation, String modelId) {
        accessor.setModelId(modelOperation, modelId);
    }

    static void setDescription(ModelOperation modelOperation, String description) {
        accessor.setDescription(modelOperation, description);
    }

    static void setCreatedOn(ModelOperation modelOperation, OffsetDateTime createdOn) {
        accessor.setCreatedOn(modelOperation, createdOn);
    }

    static void setDocTypes(ModelOperation modelOperation, Map<String, DocTypeInfo> docTypes) {
        accessor.setDocTypes(modelOperation, docTypes);
    }

    static void setError(ModelOperation modelOperation, FormRecognizerError formRecognizerError) {
        accessor.setError(modelOperation, formRecognizerError);
    }

    static void setOperationId(ModelOperation modelOperation, String operationId) {
        accessor.setOperationId(modelOperation, operationId);
    }

    static void setStatus(ModelOperation modelOperation, ModelOperationStatus status) {
        accessor.setStatus(modelOperation, status);
    }

    static void setPercentCompleted(ModelOperation modelOperation, Integer percentCompleted) {
        accessor.setPercentCompleted(modelOperation, percentCompleted);
    }

    static void setLastUpdatedOn(ModelOperation modelOperation, OffsetDateTime lastUpdatedOn) {
        accessor.setLastUpdatedOn(modelOperation, lastUpdatedOn);
    }

    static void setKind(ModelOperation modelOperation, ModelOperationKind kind) {
        accessor.setKind(modelOperation, kind);
    }

    static void setResourceLocation(ModelOperation modelOperation, String resourceLocation) {
        accessor.setResourceLocation(modelOperation, resourceLocation);
    }
}
