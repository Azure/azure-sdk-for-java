// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.implementation.util;

import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentTypeDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelDetails;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The helper class to set the non-public properties of an {@link DocumentModelDetails} instance.
 */
public final class DocumentModelDetailsHelper {
    private static DocumentModelDetailsAccessor accessor;

    private DocumentModelDetailsHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentModelDetails} instance.
     */
    public interface DocumentModelDetailsAccessor {

        void setModelId(DocumentModelDetails documentModelDetails, String modelId);

        void setDescription(DocumentModelDetails documentModelDetails, String description);

        void setCreatedOn(DocumentModelDetails documentModelDetails, OffsetDateTime createdOn);

        void setDocTypes(DocumentModelDetails documentModelDetails, Map<String, DocumentTypeDetails> docTypes);

        void setTags(DocumentModelDetails documentModelDetails, Map<String, String> tags);

        void setExpiresOn(DocumentModelDetails documentModelDetails, OffsetDateTime expiresOn);
    }

    /**
     * The method called from {@link DocumentModelDetails} to set it's accessor.
     *
     * @param documentModelDetailsAccessor The accessor.
     */
    public static void setAccessor(final DocumentModelDetailsAccessor documentModelDetailsAccessor) {
        accessor = documentModelDetailsAccessor;
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

    static void setDocTypes(DocumentModelDetails documentModelDetails, Map<String, DocumentTypeDetails> docTypes) {
        accessor.setDocTypes(documentModelDetails, docTypes);
    }

    static void setTags(DocumentModelDetails documentModelDetails, Map<String, String> tags) {
        accessor.setTags(documentModelDetails, tags);
    }

    static void setExpiresOn(DocumentModelDetails documentModelDetails, OffsetDateTime expirationDateTime) {
        accessor.setExpiresOn(documentModelDetails, expirationDateTime);
    }
}
