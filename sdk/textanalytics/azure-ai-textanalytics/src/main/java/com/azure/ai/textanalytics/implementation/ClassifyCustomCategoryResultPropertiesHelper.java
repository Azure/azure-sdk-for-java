// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ClassifyDocumentSingleCategoryResult;
import com.azure.ai.textanalytics.models.DocumentClassification;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.core.util.IterableStream;

/**
 * The helper class to set the non-public properties of an {@link ClassifyDocumentSingleCategoryResult} instance.
 */
public final class ClassifyCustomCategoryResultPropertiesHelper {
    private static ClassifyCustomCategoryResultAccessor accessor;

    private ClassifyCustomCategoryResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassifyDocumentSingleCategoryResult} instance.
     */
    public interface ClassifyCustomCategoryResultAccessor {
        void setDocumentClassification(ClassifyDocumentSingleCategoryResult classifyDocumentSingleCategoryResult,
            DocumentClassification documentClassification);
        void setWarnings(ClassifyDocumentSingleCategoryResult classifyDocumentSingleCategoryResult,
            IterableStream<TextAnalyticsWarning> warnings);
    }

    /**
     * The method called from {@link ClassifyDocumentSingleCategoryResult} to set it's accessor.
     *
     * @param classifyCustomCategoryResultAccessor The accessor.
     */
    public static void setAccessor(final ClassifyCustomCategoryResultAccessor classifyCustomCategoryResultAccessor) {
        accessor = classifyCustomCategoryResultAccessor;
    }

    public static void setDocumentClassification(ClassifyDocumentSingleCategoryResult classifyDocumentSingleCategoryResult,
        DocumentClassification documentClassification) {
        accessor.setDocumentClassification(classifyDocumentSingleCategoryResult, documentClassification);
    }

    public static void setWarnings(ClassifyDocumentSingleCategoryResult classifyDocumentSingleCategoryResult,
        IterableStream<TextAnalyticsWarning> warnings) {
        accessor.setWarnings(classifyDocumentSingleCategoryResult, warnings);
    }
}
