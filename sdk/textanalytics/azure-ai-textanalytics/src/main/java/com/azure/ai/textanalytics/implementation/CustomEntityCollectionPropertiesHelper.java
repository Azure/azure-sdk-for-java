// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.CustomEntityCollection;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.core.util.IterableStream;

/**
 * The helper class to set the non-public properties of an {@link CustomEntityCollection} instance.
 */
public final class CustomEntityCollectionPropertiesHelper {
    private static CustomEntityCollectionAccessor accessor;

    private CustomEntityCollectionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link CustomEntityCollection}
     * instance.
     */
    public interface CustomEntityCollectionAccessor {
        void setWarnings(CustomEntityCollection customEntityCollection,
            IterableStream<TextAnalyticsWarning> warnings);
    }

    /**
     * The method called from {@link CustomEntityCollection} to set it's accessor.
     *
     * @param customEntityCollectionAccessor The accessor.
     */
    public static void setAccessor(
        final CustomEntityCollectionAccessor customEntityCollectionAccessor) {
        accessor = customEntityCollectionAccessor;
    }

    public static void setWarnings(CustomEntityCollection customEntityCollection,
        IterableStream<TextAnalyticsWarning> warnings) {
        accessor.setWarnings(customEntityCollection, warnings);
    }
}
