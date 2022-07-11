// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ClassifyDocumentResult;
import com.azure.ai.textanalytics.models.ClassifiedCategory;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.core.util.IterableStream;

/**
 * The helper class to set the non-public properties of an {@link ClassifyDocumentResult} instance.
 */
public final class ClassifyDocumentResultPropertiesHelper {
    private static ClassifyDocumentResultAccessor accessor;

    private ClassifyDocumentResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassifyDocumentResult} instance.
     */
    public interface ClassifyDocumentResultAccessor {
        void setClassifiedCategories(ClassifyDocumentResult classifyDocumentResult,
            IterableStream<ClassifiedCategory> classifiedCategories);
        void setWarnings(ClassifyDocumentResult classifyDocumentResult,
            IterableStream<TextAnalyticsWarning> warnings);
    }

    /**
     * The method called from {@link ClassifyDocumentResult} to set it's accessor.
     *
     * @param classifyDocumentResultAccessor The accessor.
     */
    public static void setAccessor(final ClassifyDocumentResultAccessor classifyDocumentResultAccessor) {
        accessor = classifyDocumentResultAccessor;
    }

    public static void setClassifiedCategories(ClassifyDocumentResult classifyDocumentResult,
        IterableStream<ClassifiedCategory> classifiedCategories) {
        accessor.setClassifiedCategories(classifyDocumentResult, classifiedCategories);
    }

    public static void setWarnings(ClassifyDocumentResult classifyDocumentResult,
        IterableStream<TextAnalyticsWarning> warnings) {
        accessor.setWarnings(classifyDocumentResult, warnings);
    }
}
