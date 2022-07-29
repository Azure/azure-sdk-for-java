// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.administration.models.DocumentModelSummary;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The helper class to set the non-public properties of an {@link DocumentModelSummary} instance.
 */
public final class DocumentModelSummaryHelper {
    private static DocumentModelSummaryAccessor accessor;

    private DocumentModelSummaryHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentModelSummary} instance.
     */
    public interface DocumentModelSummaryAccessor {
        void setModelId(DocumentModelSummary documentModelSummary, String modelId);
        void setDescription(DocumentModelSummary documentModelSummary, String description);
        void setCreatedOn(DocumentModelSummary documentModelSummary, OffsetDateTime createdDateTime);
        void setTags(DocumentModelSummary documentModelSummary, Map<String, String> tags);
    }

    /**
     * The method called from {@link DocumentModelSummary} to set it's accessor.
     *
     * @param documentModelSummaryAccessor The accessor.
     */
    public static void setAccessor(final DocumentModelSummaryAccessor documentModelSummaryAccessor) {
        accessor = documentModelSummaryAccessor;
    }

    static void setModelId(DocumentModelSummary documentModelSummary, String modelId) {
        accessor.setModelId(documentModelSummary, modelId);
    }

    static void setDescription(DocumentModelSummary documentModelSummary, String description) {
        accessor.setDescription(documentModelSummary, description);
    }

    static void setCreatedOn(DocumentModelSummary documentModelSummary, OffsetDateTime createdDateTime) {
        accessor.setCreatedOn(documentModelSummary, createdDateTime);
    }

    static void setTags(DocumentModelSummary documentModelSummary, Map<String, String> tags) {
        accessor.setTags(documentModelSummary, tags);
    }
}
