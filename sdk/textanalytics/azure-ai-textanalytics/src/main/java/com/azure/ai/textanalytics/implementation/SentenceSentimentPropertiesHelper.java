// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.SentenceSentiment;

/**
 * The helper class to set the non-public properties of an {@link SentenceSentiment} instance.
 */
public final class SentenceSentimentPropertiesHelper {
    private static SentenceSentimentAccessor accessor;

    private SentenceSentimentPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link SentenceSentiment} instance.
     */
    public interface SentenceSentimentAccessor {
        void setLength(SentenceSentiment sentenceSentiment, int length);
    }

    /**
     * The method called from {@link SentenceSentiment} to set it's accessor.
     *
     * @param sentenceSentimentAccessor The accessor.
     */
    public static void setAccessor(final SentenceSentimentAccessor sentenceSentimentAccessor) {
        accessor = sentenceSentimentAccessor;
    }

    public static void setLength(SentenceSentiment sentenceSentiment, int length) {
        accessor.setLength(sentenceSentiment, length);
    }
}
