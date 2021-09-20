// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ClassifySingleCategoryResult;
import com.azure.ai.textanalytics.models.DocumentClassification;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.core.util.IterableStream;

/**
 * The helper class to set the non-public properties of an {@link ClassifySingleCategoryResult} instance.
 */
public final class ClassifyCustomCategoryResultPropertiesHelper {
    private static ClassifyCustomCategoryResultAccessor accessor;

    private ClassifyCustomCategoryResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassifySingleCategoryResult} instance.
     */
    public interface ClassifyCustomCategoryResultAccessor {
        void setDocumentClassification(ClassifySingleCategoryResult classifySingleCategoryResult,
            DocumentClassification documentClassification);
        void setWarnings(ClassifySingleCategoryResult classifySingleCategoryResult,
            IterableStream<TextAnalyticsWarning> warnings);
    }

    /**
     * The method called from {@link ClassifySingleCategoryResult} to set it's accessor.
     *
     * @param classifyCustomCategoryResultAccessor The accessor.
     */
    public static void setAccessor(final ClassifyCustomCategoryResultAccessor classifyCustomCategoryResultAccessor) {
        accessor = classifyCustomCategoryResultAccessor;
    }

    public static void setDocumentClassification(ClassifySingleCategoryResult classifySingleCategoryResult,
        DocumentClassification documentClassification) {
        accessor.setDocumentClassification(classifySingleCategoryResult, documentClassification);
    }

    public static void setWarnings(ClassifySingleCategoryResult classifySingleCategoryResult,
        IterableStream<TextAnalyticsWarning> warnings) {
        accessor.setWarnings(classifySingleCategoryResult, warnings);
    }
}
