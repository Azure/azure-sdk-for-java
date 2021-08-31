// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ClassifyCustomCategoryResult;
import com.azure.ai.textanalytics.models.DocumentClassification;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.core.util.IterableStream;

/**
 * The helper class to set the non-public properties of an {@link ClassifyCustomCategoryResult} instance.
 */
public final class ClassifyCustomCategoryResultPropertiesHelper {
    private static ClassifyCustomCategoryResultAccessor accessor;

    private ClassifyCustomCategoryResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassifyCustomCategoryResult} instance.
     */
    public interface ClassifyCustomCategoryResultAccessor {
        void setDocumentClassification(ClassifyCustomCategoryResult classifyCustomCategoryResult,
            DocumentClassification documentClassification);
        void setWarnings(ClassifyCustomCategoryResult classifyCustomCategoryResult,
            IterableStream<TextAnalyticsWarning> warnings);
    }

    /**
     * The method called from {@link ClassifyCustomCategoryResult} to set it's accessor.
     *
     * @param classifyCustomCategoryResultAccessor The accessor.
     */
    public static void setAccessor(final ClassifyCustomCategoryResultAccessor classifyCustomCategoryResultAccessor) {
        accessor = classifyCustomCategoryResultAccessor;
    }

    public static void setDocumentClassification(ClassifyCustomCategoryResult classifyCustomCategoryResult,
        DocumentClassification documentClassification) {
        accessor.setDocumentClassification(classifyCustomCategoryResult, documentClassification);
    }

    public static void setWarnings(ClassifyCustomCategoryResult classifyCustomCategoryResult,
        IterableStream<TextAnalyticsWarning> warnings) {
        accessor.setWarnings(classifyCustomCategoryResult, warnings);
    }
}
