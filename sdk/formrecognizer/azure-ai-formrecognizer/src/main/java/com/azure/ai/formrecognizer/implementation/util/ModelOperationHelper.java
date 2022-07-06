// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.administration.models.DocTypeInfo;
import com.azure.ai.formrecognizer.administration.models.DocumentModelOperationError;
import com.azure.ai.formrecognizer.administration.models.DocumentModelOperationInfo;
import com.azure.ai.formrecognizer.administration.models.ModelOperationKind;
import com.azure.ai.formrecognizer.administration.models.ModelOperationStatus;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The helper class to set the non-public properties of an {@link DocumentModelOperationInfo} instance.
 */
public final class ModelOperationHelper {
    private static ModelOperationAccessor accessor;

    private ModelOperationHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentModelOperationInfo} instance.
     */
    public interface ModelOperationAccessor {

        void setModelId(DocumentModelOperationInfo modelOperationInfo, String modelId);

        void setDescription(DocumentModelOperationInfo modelOperationInfo, String description);

        void setCreatedOn(DocumentModelOperationInfo modelOperationInfo, OffsetDateTime createdOn);

        void setDocTypes(DocumentModelOperationInfo modelOperationInfo, Map<String, DocTypeInfo> docTypes);

        void setError(DocumentModelOperationInfo modelOperationInfo, DocumentModelOperationError error);

        void setOperationId(DocumentModelOperationInfo modelOperationInfo, String operationId);

        void setStatus(DocumentModelOperationInfo modelOperationInfo, ModelOperationStatus status);

        void setPercentCompleted(DocumentModelOperationInfo modelOperationInfo, Integer percentCompleted);

        void setLastUpdatedOn(DocumentModelOperationInfo modelOperationInfo, OffsetDateTime lastUpdatedOn);

        void setKind(DocumentModelOperationInfo modelOperationInfo, ModelOperationKind kind);

        void setResourceLocation(DocumentModelOperationInfo modelOperationInfo, String resourceLocation);
    }

    /**
     * The method called from {@link DocumentModelOperationInfo} to set it's accessor.
     *
     * @param modelOperationAccessor The accessor.
     */
    public static void setAccessor(final ModelOperationHelper.ModelOperationAccessor modelOperationAccessor) {
        accessor = modelOperationAccessor;
    }

    static void setModelId(DocumentModelOperationInfo modelOperationInfo, String modelId) {
        accessor.setModelId(modelOperationInfo, modelId);
    }

    static void setDescription(DocumentModelOperationInfo modelOperationInfo, String description) {
        accessor.setDescription(modelOperationInfo, description);
    }

    static void setCreatedOn(DocumentModelOperationInfo modelOperationInfo, OffsetDateTime createdOn) {
        accessor.setCreatedOn(modelOperationInfo, createdOn);
    }

    static void setDocTypes(DocumentModelOperationInfo modelOperationInfo, Map<String, DocTypeInfo> docTypes) {
        accessor.setDocTypes(modelOperationInfo, docTypes);
    }

    static void setError(DocumentModelOperationInfo modelOperationInfo, DocumentModelOperationError documentModelOperationError) {
        accessor.setError(modelOperationInfo, documentModelOperationError);
    }

    static void setOperationId(DocumentModelOperationInfo modelOperationInfo, String operationId) {
        accessor.setOperationId(modelOperationInfo, operationId);
    }

    static void setStatus(DocumentModelOperationInfo modelOperationInfo, ModelOperationStatus status) {
        accessor.setStatus(modelOperationInfo, status);
    }

    static void setPercentCompleted(DocumentModelOperationInfo modelOperationInfo, Integer percentCompleted) {
        accessor.setPercentCompleted(modelOperationInfo, percentCompleted);
    }

    static void setLastUpdatedOn(DocumentModelOperationInfo modelOperationInfo, OffsetDateTime lastUpdatedOn) {
        accessor.setLastUpdatedOn(modelOperationInfo, lastUpdatedOn);
    }

    static void setKind(DocumentModelOperationInfo modelOperationInfo, ModelOperationKind kind) {
        accessor.setKind(modelOperationInfo, kind);
    }

    static void setResourceLocation(DocumentModelOperationInfo modelOperationInfo, String resourceLocation) {
        accessor.setResourceLocation(modelOperationInfo, resourceLocation);
    }
}
