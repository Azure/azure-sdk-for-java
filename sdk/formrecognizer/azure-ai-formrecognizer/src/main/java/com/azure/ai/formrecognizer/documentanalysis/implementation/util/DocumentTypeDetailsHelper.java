// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.implementation.util;

import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentTypeDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelBuildMode;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentFieldSchema;

import java.util.Map;

/**
 * The helper class to set the non-public properties of an {@link DocumentTypeDetails} instance.
 */
public final class DocumentTypeDetailsHelper {
    private static DocumentTypeDetailsAccessor accessor;

    private DocumentTypeDetailsHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentTypeDetails} instance.
     */
    public interface DocumentTypeDetailsAccessor {
        void setDescription(DocumentTypeDetails documentTypeDetails, String description);
        void setFieldSchema(DocumentTypeDetails documentTypeDetails, Map<String, DocumentFieldSchema> fieldSchema);
        void setFieldConfidence(DocumentTypeDetails documentTypeDetails, Map<String, Float> fieldConfidence);
        void setBuildMode(DocumentTypeDetails documentTypeDetails, DocumentModelBuildMode buildMode);
    }

    /**
     * The method called from {@link DocumentTypeDetails} to set it's accessor.
     *
     * @param docInfoAccessor The accessor.
     */
    public static void setAccessor(final DocumentTypeDetailsAccessor docInfoAccessor) {
        accessor = docInfoAccessor;
    }

    static void setDescription(DocumentTypeDetails documentTypeDetails, String description) {
        accessor.setDescription(documentTypeDetails, description);
    }

    static void setFieldSchema(DocumentTypeDetails documentTypeDetails, Map<String, DocumentFieldSchema> fieldSchema) {
        accessor.setFieldSchema(documentTypeDetails, fieldSchema);
    }

    static void setFieldConfidence(DocumentTypeDetails documentTypeDetails, Map<String, Float> fieldConfidence) {
        accessor.setFieldConfidence(documentTypeDetails, fieldConfidence);
    }

    static void setBuildMode(DocumentTypeDetails documentTypeDetails, DocumentModelBuildMode buildMode) {
        accessor.setBuildMode(documentTypeDetails, buildMode);
    }
}
