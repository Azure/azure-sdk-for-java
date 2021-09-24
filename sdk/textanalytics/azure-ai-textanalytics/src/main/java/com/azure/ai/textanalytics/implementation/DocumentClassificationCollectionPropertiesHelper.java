// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ClassificationCategoryCollection;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.core.util.IterableStream;

/**
 * The helper class to set the non-public properties of an {@link ClassificationCategoryCollection} instance.
 */
public final class DocumentClassificationCollectionPropertiesHelper {
    private static DocumentClassificationCollectionAccessor accessor;

    private DocumentClassificationCollectionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassificationCategoryCollection}
     * instance.
     */
    public interface DocumentClassificationCollectionAccessor {
        void setWarnings(ClassificationCategoryCollection documentClassifications,
            IterableStream<TextAnalyticsWarning> warnings);
    }

    /**
     * The method called from {@link ClassificationCategoryCollection} to set it's accessor.
     *
     * @param documentClassificationCollectionAccessor The accessor.
     */
    public static void setAccessor(
        final DocumentClassificationCollectionAccessor documentClassificationCollectionAccessor) {
        accessor = documentClassificationCollectionAccessor;
    }

    public static void setWarnings(ClassificationCategoryCollection classificationCategoryCollection,
        IterableStream<TextAnalyticsWarning> warnings) {
        accessor.setWarnings(classificationCategoryCollection, warnings);
    }
}
