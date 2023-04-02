// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.implementation.util;

import com.azure.ai.formrecognizer.documentanalysis.administration.models.ClassifierDocumentTypeDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentClassifierDetails;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The helper class to set the non-public properties of an {@link DocumentClassifierDetails} instance.
 */
public final class DocumentClassifierDetailsHelper {
    private static DocumentClassifierDetailsAccessor accessor;

    private DocumentClassifierDetailsHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentClassifierDetails} instance.
     */
    public interface DocumentClassifierDetailsAccessor {
        void setClassifierId(DocumentClassifierDetails documentClassifierDetails, String modelId);
        void setDescription(DocumentClassifierDetails documentClassifierDetails, String description);
        void setApiVersion(DocumentClassifierDetails documentClassifierDetails, String apiVersion);
        void setCreatedOn(DocumentClassifierDetails documentClassifierDetails, OffsetDateTime createdDateTime);
        void setExpiresOn(DocumentClassifierDetails documentClassifierDetails, OffsetDateTime expirationDateTime);
        void setDocTypes(DocumentClassifierDetails documentClassifierDetails, Map<String, ClassifierDocumentTypeDetails> docTypes);
    }

    /**
     * The method called from {@link DocumentClassifierDetails} to set it's accessor.
     *
     * @param documentClassifierDetailsAccessor The accessor.
     */
    public static void setAccessor(final DocumentClassifierDetailsAccessor documentClassifierDetailsAccessor) {
        accessor = documentClassifierDetailsAccessor;
    }

    static void setClassifierId(DocumentClassifierDetails documentClassifierDetails, String classifierId) {
        accessor.setClassifierId(documentClassifierDetails, classifierId);
    }

    static void setApiVersion(DocumentClassifierDetails documentClassifierDetails, String apiVersion) {
        accessor.setApiVersion(documentClassifierDetails, apiVersion);
    }

    static void setDescription(DocumentClassifierDetails documentClassifierDetails, String description) {
        accessor.setDescription(documentClassifierDetails, description);
    }

    static void setCreatedOn(DocumentClassifierDetails documentClassifierDetails, OffsetDateTime createdDateTime) {
        accessor.setCreatedOn(documentClassifierDetails, createdDateTime);
    }

    static void setExpiresOn(DocumentClassifierDetails documentClassifierDetails, OffsetDateTime expirationDateTime) {
        accessor.setExpiresOn(documentClassifierDetails, expirationDateTime);
    }

    static void setDocTypes(DocumentClassifierDetails documentClassifierDetails, Map<String, ClassifierDocumentTypeDetails> docTypes) {
        accessor.setDocTypes(documentClassifierDetails, docTypes);
    }
}
