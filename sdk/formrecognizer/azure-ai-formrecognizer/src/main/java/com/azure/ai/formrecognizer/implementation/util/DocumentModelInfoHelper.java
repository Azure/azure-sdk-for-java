// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.administration.models.DocTypeInfo;
import com.azure.ai.formrecognizer.administration.models.DocumentModelInfo;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The helper class to set the non-public properties of an {@link DocumentModelInfo} instance.
 */
public final class DocumentModelInfoHelper {
    private static DocumentModelInfoAccessor accessor;

    private DocumentModelInfoHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentModelInfo} instance.
     */
    public interface DocumentModelInfoAccessor {

        void setModelId(DocumentModelInfo documentModelInfo, String modelId);

        void setDescription(DocumentModelInfo documentModelInfo, String description);

        void setCreatedOn(DocumentModelInfo documentModelInfo, OffsetDateTime createdOn);

        void setDocTypes(DocumentModelInfo documentModelInfo, Map<String, DocTypeInfo> docTypes);

        void setTags(DocumentModelInfo documentModelInfo, Map<String, String> tags);
    }

    /**
     * The method called from {@link DocumentModelInfo} to set it's accessor.
     *
     * @param documentModelInfoAccessor The accessor.
     */
    public static void setAccessor(final DocumentModelInfoAccessor documentModelInfoAccessor) {
        accessor = documentModelInfoAccessor;
    }

    static void setModelId(DocumentModelInfo documentModelInfo, String modelId) {
        accessor.setModelId(documentModelInfo, modelId);
    }

    static void setDescription(DocumentModelInfo documentModelInfo, String description) {
        accessor.setDescription(documentModelInfo, description);
    }

    static void setCreatedOn(DocumentModelInfo documentModelInfo, OffsetDateTime createdOn) {
        accessor.setCreatedOn(documentModelInfo, createdOn);
    }

    static void setDocTypes(DocumentModelInfo documentModelInfo, Map<String, DocTypeInfo> docTypes) {
        accessor.setDocTypes(documentModelInfo, docTypes);
    }

    static void setTags(DocumentModelInfo documentModelInfo, Map<String, String> tags) {
        accessor.setTags(documentModelInfo, tags);
    }
}
