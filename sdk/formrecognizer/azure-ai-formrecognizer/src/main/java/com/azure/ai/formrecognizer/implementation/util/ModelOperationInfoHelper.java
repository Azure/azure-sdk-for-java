// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.administration.models.DocumentModelOperationSummary;
import com.azure.ai.formrecognizer.administration.models.ModelOperationKind;
import com.azure.ai.formrecognizer.administration.models.ModelOperationStatus;

import java.time.OffsetDateTime;

/**
 * The helper class to set the non-public properties of an {@link DocumentModelOperationSummary} instance.
 */
public final class ModelOperationInfoHelper {
    private static ModelOperationInfoAccessor accessor;

    private ModelOperationInfoHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentModelOperationSummary} instance.
     */
    public interface ModelOperationInfoAccessor {

        void setOperationId(DocumentModelOperationSummary documentModelOperationSummary, String operationId);

        void setStatus(DocumentModelOperationSummary documentModelOperationSummary, ModelOperationStatus status);

        void setPercentCompleted(DocumentModelOperationSummary documentModelOperationSummary, Integer percentCompleted);

        void setCreatedOn(DocumentModelOperationSummary documentModelOperationSummary, OffsetDateTime createdOn);

        void setLastUpdatedOn(DocumentModelOperationSummary documentModelOperationSummary, OffsetDateTime lastUpdatedOn);

        void setKind(DocumentModelOperationSummary documentModelOperationSummary, ModelOperationKind kind);

        void setResourceLocation(DocumentModelOperationSummary documentModelOperationSummary, String resourceLocation);
    }

    /**
     * The method called from {@link DocumentModelOperationSummary} to set it's accessor.
     *
     * @param modelOperationInfoAccessor The accessor.
     */
    public static void setAccessor(
        final ModelOperationInfoHelper.ModelOperationInfoAccessor modelOperationInfoAccessor) {
        accessor = modelOperationInfoAccessor;
    }

    static void setOperationId(DocumentModelOperationSummary documentModelOperationSummary, String operationId) {
        accessor.setOperationId(documentModelOperationSummary, operationId);
    }

    static void setStatus(DocumentModelOperationSummary documentModelOperationSummary, ModelOperationStatus status) {
        accessor.setStatus(documentModelOperationSummary, status);
    }

    static void setPercentCompleted(DocumentModelOperationSummary documentModelOperationSummary, Integer percentCompleted) {
        accessor.setPercentCompleted(documentModelOperationSummary, percentCompleted);
    }

    static void setCreatedOn(DocumentModelOperationSummary documentModelOperationSummary, OffsetDateTime createdOn) {
        accessor.setCreatedOn(documentModelOperationSummary, createdOn);
    }

    static void setLastUpdatedOn(DocumentModelOperationSummary documentModelOperationSummary, OffsetDateTime lastUpdatedOn) {
        accessor.setLastUpdatedOn(documentModelOperationSummary, lastUpdatedOn);
    }

    static void setKind(DocumentModelOperationSummary documentModelOperationSummary, ModelOperationKind kind) {
        accessor.setKind(documentModelOperationSummary, kind);
    }

    static void setResourceLocation(DocumentModelOperationSummary documentModelOperationSummary, String resourceLocation) {
        accessor.setResourceLocation(documentModelOperationSummary, resourceLocation);
    }
}
