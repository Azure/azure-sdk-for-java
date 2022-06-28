// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.LabelClassifyResult;
import com.azure.ai.textanalytics.models.ClassifiedCategory;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.core.util.IterableStream;

/**
 * The helper class to set the non-public properties of an {@link LabelClassifyResult} instance.
 */
public final class LabelClassifyResultPropertiesHelper {
    private static LabelClassifyResultAccessor accessor;

    private LabelClassifyResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link LabelClassifyResult} instance.
     */
    public interface LabelClassifyResultAccessor {
        void setClassifiedCategories(LabelClassifyResult labelClassifyResult,
            IterableStream<ClassifiedCategory> classifiedCategories);
        void setWarnings(LabelClassifyResult labelClassifyResult,
            IterableStream<TextAnalyticsWarning> warnings);
    }

    /**
     * The method called from {@link LabelClassifyResult} to set it's accessor.
     *
     * @param labelClassifyResultAccessor The accessor.
     */
    public static void setAccessor(final LabelClassifyResultAccessor labelClassifyResultAccessor) {
        accessor = labelClassifyResultAccessor;
    }

    public static void setClassifications(LabelClassifyResult labelClassifyResult,
        IterableStream<ClassifiedCategory> classifications) {
        accessor.setClassifiedCategories(labelClassifyResult, classifications);
    }

    public static void setWarnings(LabelClassifyResult labelClassifyResult,
        IterableStream<TextAnalyticsWarning> warnings) {
        accessor.setWarnings(labelClassifyResult, warnings);
    }
}
