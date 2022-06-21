// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.LabelClassificationResult;
import com.azure.ai.textanalytics.models.ClassificationCategory;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.core.util.IterableStream;

/**
 * The helper class to set the non-public properties of an {@link LabelClassificationResult} instance.
 */
public final class LabelClassificationResultPropertiesHelper {
    private static LabelClassificationResultAccessor accessor;

    private LabelClassificationResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link LabelClassificationResult} instance.
     */
    public interface LabelClassificationResultAccessor {
        void setClassifications(LabelClassificationResult labelClassificationResult,
            IterableStream<ClassificationCategory> classifications);
        void setWarnings(LabelClassificationResult labelClassificationResult,
            IterableStream<TextAnalyticsWarning> warnings);
    }

    /**
     * The method called from {@link LabelClassificationResult} to set it's accessor.
     *
     * @param labelClassificationResultAccessor The accessor.
     */
    public static void setAccessor(final LabelClassificationResultAccessor labelClassificationResultAccessor) {
        accessor = labelClassificationResultAccessor;
    }

    public static void setClassifications(LabelClassificationResult labelClassificationResult,
        IterableStream<ClassificationCategory> classifications) {
        accessor.setClassifications(labelClassificationResult, classifications);
    }

    public static void setWarnings(LabelClassificationResult labelClassificationResult,
        IterableStream<TextAnalyticsWarning> warnings) {
        accessor.setWarnings(labelClassificationResult, warnings);
    }
}
