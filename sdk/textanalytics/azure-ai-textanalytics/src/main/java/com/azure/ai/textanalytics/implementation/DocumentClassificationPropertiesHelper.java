// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.DocumentClassification;

/**
 * The helper class to set the non-public properties of an {@link DocumentClassification} instance.
 */
public final class DocumentClassificationPropertiesHelper {
    private static DocumentClassificationAccessor accessor;

    private DocumentClassificationPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentClassification} instance.
     */
    public interface DocumentClassificationAccessor {
        void setCategory(DocumentClassification documentClassification, String category);
        void setConfidenceScore(DocumentClassification documentClassification, double confidenceScore);
    }

    /**
     * The method called from {@link DocumentClassification} to set it's accessor.
     *
     * @param documentClassificationAccessor The accessor.
     */
    public static void setAccessor(final DocumentClassificationAccessor documentClassificationAccessor) {
        accessor = documentClassificationAccessor;
    }

    public static void setCategory(DocumentClassification documentClassification, String category) {
        accessor.setCategory(documentClassification, category);
    }

    public static void setConfidenceScore(DocumentClassification documentClassification, double confidenceScore) {
        accessor.setConfidenceScore(documentClassification, confidenceScore);
    }
}
