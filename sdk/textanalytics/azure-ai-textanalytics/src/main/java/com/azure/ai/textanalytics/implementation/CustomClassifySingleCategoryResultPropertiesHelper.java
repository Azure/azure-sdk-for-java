// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.CustomClassifySingleCategoryResult;
import com.azure.ai.textanalytics.models.DocumentClassification;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.core.util.IterableStream;

/**
 * The helper class to set the non-public properties of an {@link CustomClassifySingleCategoryResult} instance.
 */
public final class CustomClassifySingleCategoryResultPropertiesHelper {
    private static ClassifySingleCategoryResultAccessor accessor;

    private CustomClassifySingleCategoryResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link CustomClassifySingleCategoryResult} instance.
     */
    public interface ClassifySingleCategoryResultAccessor {
        void setDocumentClassification(CustomClassifySingleCategoryResult customClassifySingleCategoryResult,
            DocumentClassification documentClassification);
        void setWarnings(CustomClassifySingleCategoryResult customClassifySingleCategoryResult,
            IterableStream<TextAnalyticsWarning> warnings);
    }

    /**
     * The method called from {@link CustomClassifySingleCategoryResult} to set it's accessor.
     *
     * @param classifySingleCategoryResultAccessor The accessor.
     */
    public static void setAccessor(final ClassifySingleCategoryResultAccessor classifySingleCategoryResultAccessor) {
        accessor = classifySingleCategoryResultAccessor;
    }

    public static void setDocumentClassification(CustomClassifySingleCategoryResult customClassifySingleCategoryResult,
        DocumentClassification documentClassification) {
        accessor.setDocumentClassification(customClassifySingleCategoryResult, documentClassification);
    }

    public static void setWarnings(CustomClassifySingleCategoryResult customClassifySingleCategoryResult,
        IterableStream<TextAnalyticsWarning> warnings) {
        accessor.setWarnings(customClassifySingleCategoryResult, warnings);
    }
}
