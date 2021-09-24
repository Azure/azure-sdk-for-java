// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.SingleCategoryClassifyResult;
import com.azure.ai.textanalytics.models.ClassificationCategory;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.core.util.IterableStream;

/**
 * The helper class to set the non-public properties of an {@link SingleCategoryClassifyResult} instance.
 */
public final class ClassifyCustomCategoryResultPropertiesHelper {
    private static ClassifyCustomCategoryResultAccessor accessor;

    private ClassifyCustomCategoryResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link SingleCategoryClassifyResult} instance.
     */
    public interface ClassifyCustomCategoryResultAccessor {
        void setClassification(SingleCategoryClassifyResult singleCategoryClassifyResult,
            ClassificationCategory classification);
        void setWarnings(SingleCategoryClassifyResult singleCategoryClassifyResult,
            IterableStream<TextAnalyticsWarning> warnings);
    }

    /**
     * The method called from {@link SingleCategoryClassifyResult} to set it's accessor.
     *
     * @param classifyCustomCategoryResultAccessor The accessor.
     */
    public static void setAccessor(final ClassifyCustomCategoryResultAccessor classifyCustomCategoryResultAccessor) {
        accessor = classifyCustomCategoryResultAccessor;
    }

    public static void setDocumentClassification(SingleCategoryClassifyResult singleCategoryClassifyResult,
        ClassificationCategory classificationCategory) {
        accessor.setClassification(singleCategoryClassifyResult, classificationCategory);
    }

    public static void setWarnings(SingleCategoryClassifyResult singleCategoryClassifyResult,
        IterableStream<TextAnalyticsWarning> warnings) {
        accessor.setWarnings(singleCategoryClassifyResult, warnings);
    }
}
