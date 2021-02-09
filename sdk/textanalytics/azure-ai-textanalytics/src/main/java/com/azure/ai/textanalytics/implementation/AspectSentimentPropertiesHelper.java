// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AspectSentiment;

/**
 * The helper class to set the non-public properties of an {@link AspectSentiment} instance.
 */
public final class AspectSentimentPropertiesHelper {
    private static AspectSentimentAccessor accessor;

    private AspectSentimentPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AspectSentiment} instance.
     */
    public interface AspectSentimentAccessor {
        void setLength(AspectSentiment aspectSentiment, int length);
    }

    /**
     * The method called from {@link AspectSentiment} to set it's accessor.
     *
     * @param aspectSentimentAccessor The accessor.
     */
    public static void setAccessor(final AspectSentimentAccessor aspectSentimentAccessor) {
        accessor = aspectSentimentAccessor;
    }

    public static void setLength(AspectSentiment aspectSentiment, int length) {
        accessor.setLength(aspectSentiment, length);
    }
}
