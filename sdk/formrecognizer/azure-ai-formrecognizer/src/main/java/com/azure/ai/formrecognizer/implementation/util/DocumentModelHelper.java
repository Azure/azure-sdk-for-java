// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.administration.models.DocTypeInfo;
import com.azure.ai.formrecognizer.administration.models.DocumentModel;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The helper class to set the non-public properties of an {@link DocumentModel} instance.
 */
public final class DocumentModelHelper {
    private static DocumentModelAccessor accessor;

    private DocumentModelHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentModel} instance.
     */
    public interface DocumentModelAccessor {

        void setModelId(DocumentModel documentModel, String modelId);

        void setDescription(DocumentModel documentModel, String description);

        void setCreatedOn(DocumentModel documentModel, OffsetDateTime createdOn);

        void setDocTypes(DocumentModel documentModel, Map<String, DocTypeInfo> docTypes);

    }

    /**
     * The method called from {@link DocumentModel} to set it's accessor.
     *
     * @param documentModelAccessor The accessor.
     */
    public static void setAccessor(final DocumentModelHelper.DocumentModelAccessor documentModelAccessor) {
        accessor = documentModelAccessor;
    }

    static void setModelId(DocumentModel documentModel, String modelId) {
        accessor.setModelId(documentModel, modelId);
    }

    static void setDescription(DocumentModel documentModel, String description) {
        accessor.setDescription(documentModel, description);
    }

    static void setCreatedOn(DocumentModel documentModel, OffsetDateTime createdOn) {
        accessor.setCreatedOn(documentModel, createdOn);
    }

    static void setDocTypes(DocumentModel documentModel, Map<String, DocTypeInfo> docTypes) {
        accessor.setDocTypes(documentModel, docTypes);
    }
}
