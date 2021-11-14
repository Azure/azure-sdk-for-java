// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ClassificationCategoryCollection;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.core.util.IterableStream;

/**
 * The helper class to set the non-public properties of an {@link ClassificationCategoryCollection} instance.
 */
public final class ClassificationCategoryCollectionPropertiesHelper {
    private static ClassificationCategoryCollectionAccessor accessor;

    private ClassificationCategoryCollectionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassificationCategoryCollection}
     * instance.
     */
    public interface ClassificationCategoryCollectionAccessor {
        void setWarnings(ClassificationCategoryCollection classifications,
            IterableStream<TextAnalyticsWarning> warnings);
    }

    /**
     * The method called from {@link ClassificationCategoryCollection} to set it's accessor.
     *
     * @param classificationCategoryCollectionAccessor The accessor.
     */
    public static void setAccessor(
        final ClassificationCategoryCollectionAccessor classificationCategoryCollectionAccessor) {
        accessor = classificationCategoryCollectionAccessor;
    }

    public static void setWarnings(ClassificationCategoryCollection classifications,
        IterableStream<TextAnalyticsWarning> warnings) {
        accessor.setWarnings(classifications, warnings);
    }
}
