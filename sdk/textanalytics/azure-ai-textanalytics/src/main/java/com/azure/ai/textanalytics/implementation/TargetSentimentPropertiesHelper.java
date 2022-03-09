// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.SentimentConfidenceScores;
import com.azure.ai.textanalytics.models.TargetSentiment;
import com.azure.ai.textanalytics.models.TextSentiment;

/**
 * The helper class to set the non-public properties of an {@link TargetSentiment} instance.
 */
public final class TargetSentimentPropertiesHelper {
    private static TargetSentimentAccessor accessor;

    private TargetSentimentPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link TargetSentiment} instance.
     */
    public interface TargetSentimentAccessor {
        void setText(TargetSentiment targetSentiment, String text);
        void setSentiment(TargetSentiment targetSentiment, TextSentiment sentiment);
        void setConfidenceScores(TargetSentiment targetSentiment, SentimentConfidenceScores confidenceScores);
        void setOffset(TargetSentiment targetSentiment, int offset);
        void setLength(TargetSentiment targetSentiment, int length);
    }

    /**
     * The method called from {@link TargetSentiment} to set it's accessor.
     *
     * @param targetSentimentAccessor The accessor.
     */
    public static void setAccessor(final TargetSentimentAccessor targetSentimentAccessor) {
        accessor = targetSentimentAccessor;
    }

    public static void setText(TargetSentiment targetSentiment, String text) {
        accessor.setText(targetSentiment, text);
    }

    public static void setSentiment(TargetSentiment targetSentiment, TextSentiment sentiment) {
        accessor.setSentiment(targetSentiment, sentiment);
    }

    public static void setConfidenceScores(TargetSentiment targetSentiment,
        SentimentConfidenceScores confidenceScores) {
        accessor.setConfidenceScores(targetSentiment, confidenceScores);
    }

    public static void setOffset(TargetSentiment targetSentiment, int offset) {
        accessor.setOffset(targetSentiment, offset);
    }

    public static void setLength(TargetSentiment targetSentiment, int length) {
        accessor.setLength(targetSentiment, length);
    }
}
