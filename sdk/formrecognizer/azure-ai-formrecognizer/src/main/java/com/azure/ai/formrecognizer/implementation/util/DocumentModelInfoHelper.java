// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.administration.models.DocTypeInfo;
import com.azure.ai.formrecognizer.administration.models.DocumentModelDetails;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The helper class to set the non-public properties of an {@link DocumentModelDetails} instance.
 */
public final class DocumentModelInfoHelper {
    private static DocumentModelInfoAccessor accessor;

    private DocumentModelInfoHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentModelDetails} instance.
     */
    public interface DocumentModelInfoAccessor {

        void setModelId(DocumentModelDetails documentModelDetails, String modelId);

        void setDescription(DocumentModelDetails documentModelDetails, String description);

        void setCreatedOn(DocumentModelDetails documentModelDetails, OffsetDateTime createdOn);

        void setDocTypes(DocumentModelDetails documentModelDetails, Map<String, DocTypeInfo> docTypes);

        void setTags(DocumentModelDetails documentModelDetails, Map<String, String> tags);
    }

    /**
     * The method called from {@link DocumentModelDetails} to set it's accessor.
     *
     * @param documentModelInfoAccessor The accessor.
     */
    public static void setAccessor(final DocumentModelInfoAccessor documentModelInfoAccessor) {
        accessor = documentModelInfoAccessor;
    }

    static void setModelId(DocumentModelDetails documentModelDetails, String modelId) {
        accessor.setModelId(documentModelDetails, modelId);
    }

    static void setDescription(DocumentModelDetails documentModelDetails, String description) {
        accessor.setDescription(documentModelDetails, description);
    }

    static void setCreatedOn(DocumentModelDetails documentModelDetails, OffsetDateTime createdOn) {
        accessor.setCreatedOn(documentModelDetails, createdOn);
    }

    static void setDocTypes(DocumentModelDetails documentModelDetails, Map<String, DocTypeInfo> docTypes) {
        accessor.setDocTypes(documentModelDetails, docTypes);
    }

    static void setTags(DocumentModelDetails documentModelDetails, Map<String, String> tags) {
        accessor.setTags(documentModelDetails, tags);
    }
}
