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
public final class SingleCategoryClassifyResultPropertiesHelper {
    private static SingleCategoryClassifyResultAccessor accessor;

    private SingleCategoryClassifyResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link SingleCategoryClassifyResult} instance.
     */
    public interface SingleCategoryClassifyResultAccessor {
        void setClassification(SingleCategoryClassifyResult singleCategoryClassifyResult,
            ClassificationCategory classification);
        void setWarnings(SingleCategoryClassifyResult singleCategoryClassifyResult,
            IterableStream<TextAnalyticsWarning> warnings);
    }

    /**
     * The method called from {@link SingleCategoryClassifyResult} to set it's accessor.
     *
     * @param singleCategoryClassifyResultAccessor The accessor.
     */
    public static void setAccessor(final SingleCategoryClassifyResultAccessor singleCategoryClassifyResultAccessor) {
        accessor = singleCategoryClassifyResultAccessor;
    }

    public static void setClassification(SingleCategoryClassifyResult singleCategoryClassifyResult,
        ClassificationCategory classification) {
        accessor.setClassification(singleCategoryClassifyResult, classification);
    }

    public static void setWarnings(SingleCategoryClassifyResult singleCategoryClassifyResult,
        IterableStream<TextAnalyticsWarning> warnings) {
        accessor.setWarnings(singleCategoryClassifyResult, warnings);
    }
}
