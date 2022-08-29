// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.implementation.util;

import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelOperationDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentOperationKind;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentOperationStatus;
import com.azure.core.models.ResponseError;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The helper class to set the non-public properties of an {@link DocumentModelOperationDetails} instance.
 */
public final class DocumentModelOperationDetailsHelper {
    private static DocumentModelOperationDetailsAccessor accessor;

    private DocumentModelOperationDetailsHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentModelOperationDetails} instance.
     */
    public interface DocumentModelOperationDetailsAccessor {

        void setCreatedOn(DocumentModelOperationDetails documentModelOperationDetails, OffsetDateTime createdOn);

        void setError(DocumentModelOperationDetails documentModelOperationDetails, ResponseError error);

        void setOperationId(DocumentModelOperationDetails documentModelOperationDetails, String operationId);

        void setStatus(DocumentModelOperationDetails documentModelOperationDetails, DocumentOperationStatus status);

        void setPercentCompleted(DocumentModelOperationDetails documentModelOperationDetails, Integer percentCompleted);

        void setLastUpdatedOn(DocumentModelOperationDetails documentModelOperationDetails, OffsetDateTime lastUpdatedOn);

        void setKind(DocumentModelOperationDetails documentModelOperationDetails, DocumentOperationKind kind);

        void setResourceLocation(DocumentModelOperationDetails documentModelOperationDetails, String resourceLocation);
        void setTags(DocumentModelOperationDetails documentModelOperationDetails, Map<String, String> tags);
        void setResult(DocumentModelOperationDetails documentModelOperationDetails, DocumentModelDetails documentModelDetails);
    }

    /**
     * The method called from {@link DocumentModelOperationDetails} to set it's accessor.
     *
     * @param modelOperationDetailsAccessor The accessor.
     */
    public static void setAccessor(final DocumentModelOperationDetailsAccessor modelOperationDetailsAccessor) {
        accessor = modelOperationDetailsAccessor;
    }

    static void setCreatedOn(DocumentModelOperationDetails documentModelOperationDetails, OffsetDateTime createdOn) {
        accessor.setCreatedOn(documentModelOperationDetails, createdOn);
    }

    static void setError(DocumentModelOperationDetails documentModelOperationDetails, ResponseError responseError) {
        accessor.setError(documentModelOperationDetails, responseError);
    }

    static void setOperationId(DocumentModelOperationDetails documentModelOperationDetails, String operationId) {
        accessor.setOperationId(documentModelOperationDetails, operationId);
    }

    static void setStatus(DocumentModelOperationDetails documentModelOperationDetails, DocumentOperationStatus status) {
        accessor.setStatus(documentModelOperationDetails, status);
    }

    static void setPercentCompleted(DocumentModelOperationDetails documentModelOperationDetails, Integer percentCompleted) {
        accessor.setPercentCompleted(documentModelOperationDetails, percentCompleted);
    }

    static void setLastUpdatedOn(DocumentModelOperationDetails documentModelOperationDetails, OffsetDateTime lastUpdatedOn) {
        accessor.setLastUpdatedOn(documentModelOperationDetails, lastUpdatedOn);
    }

    static void setKind(DocumentModelOperationDetails documentModelOperationDetails, DocumentOperationKind kind) {
        accessor.setKind(documentModelOperationDetails, kind);
    }

    static void setResourceLocation(DocumentModelOperationDetails documentModelOperationDetails, String resourceLocation) {
        accessor.setResourceLocation(documentModelOperationDetails, resourceLocation);
    }
    static void setTags(DocumentModelOperationDetails documentModelOperationDetails, Map<String, String> tags) {
        accessor.setTags(documentModelOperationDetails, tags);
    }

    static void setResult(DocumentModelOperationDetails documentModelOperationDetails, DocumentModelDetails documentModelDetails) {
        accessor.setResult(documentModelOperationDetails, documentModelDetails);
    }
}
