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
public final class ClassifySingleCategoryResultPropertiesHelper {
    private static ClassifySingleCategoryResultAccessor accessor;

    private ClassifySingleCategoryResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassifySingleCategoryResult} instance.
     */
    public interface ClassifySingleCategoryResultAccessor {
        void setDocumentClassification(ClassifySingleCategoryResult classifySingleCategoryResult,
            DocumentClassification documentClassification);
        void setWarnings(ClassifySingleCategoryResult classifySingleCategoryResult,
            IterableStream<TextAnalyticsWarning> warnings);
    }

    /**
     * The method called from {@link ClassifySingleCategoryResult} to set it's accessor.
     *
     * @param classifySingleCategoryResultAccessor The accessor.
     */
    public static void setAccessor(final ClassifySingleCategoryResultAccessor classifySingleCategoryResultAccessor) {
        accessor = classifySingleCategoryResultAccessor;
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
