// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ClassifyCustomSingleCategoryResult;
import com.azure.ai.textanalytics.models.DocumentClassification;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.core.util.IterableStream;

/**
 * The helper class to set the non-public properties of an {@link ClassifyCustomSingleCategoryResult} instance.
 */
public final class ClassifyCustomCategoryResultPropertiesHelper {
    private static ClassifyCustomCategoryResultAccessor accessor;

    private ClassifyCustomCategoryResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassifyCustomSingleCategoryResult} instance.
     */
    public interface ClassifyCustomCategoryResultAccessor {
        void setDocumentClassification(ClassifyCustomSingleCategoryResult classifyCustomSingleCategoryResult,
            DocumentClassification documentClassification);
        void setWarnings(ClassifyCustomSingleCategoryResult classifyCustomSingleCategoryResult,
            IterableStream<TextAnalyticsWarning> warnings);
    }

    /**
     * The method called from {@link ClassifyCustomSingleCategoryResult} to set it's accessor.
     *
     * @param classifyCustomCategoryResultAccessor The accessor.
     */
    public static void setAccessor(final ClassifyCustomCategoryResultAccessor classifyCustomCategoryResultAccessor) {
        accessor = classifyCustomCategoryResultAccessor;
    }

    public static void setDocumentClassification(ClassifyCustomSingleCategoryResult classifyCustomSingleCategoryResult,
        DocumentClassification documentClassification) {
        accessor.setDocumentClassification(classifyCustomSingleCategoryResult, documentClassification);
    }

    public static void setWarnings(ClassifyCustomSingleCategoryResult classifyCustomSingleCategoryResult,
        IterableStream<TextAnalyticsWarning> warnings) {
        accessor.setWarnings(classifyCustomSingleCategoryResult, warnings);
    }
}
