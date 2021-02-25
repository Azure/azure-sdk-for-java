// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.SentenceOpinion;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.core.util.IterableStream;

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
        void setOpinions(SentenceSentiment sentenceSentiment, IterableStream<SentenceOpinion> opinions);
        void setOffset(SentenceSentiment sentenceSentiment, int offset);
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

    public static void setOpinions(SentenceSentiment sentenceSentiment, IterableStream<SentenceOpinion> opinions) {
        accessor.setOpinions(sentenceSentiment, opinions);
    }

    public static void setOffset(SentenceSentiment sentenceSentiment, int offset) {
        accessor.setOffset(sentenceSentiment, offset);
    }

    public static void setLength(SentenceSentiment sentenceSentiment, int length) {
        accessor.setLength(sentenceSentiment, length);
    }
}
