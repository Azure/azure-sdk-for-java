// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AssessmentSentiment;
import com.azure.ai.textanalytics.models.SentimentConfidenceScores;
import com.azure.ai.textanalytics.models.TextSentiment;

/**
 * The helper class to set the non-public properties of an {@link AssessmentSentiment} instance.
 */
public final class AssessmentSentimentPropertiesHelper {
    private static AssessmentSentimentAccessor accessor;

    private AssessmentSentimentPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AssessmentSentiment} instance.
     */
    public interface AssessmentSentimentAccessor {
        void setText(AssessmentSentiment assessmentSentiment, String text);
        void setSentiment(AssessmentSentiment assessmentSentiment, TextSentiment sentiment);
        void setConfidenceScores(AssessmentSentiment assessmentSentiment, SentimentConfidenceScores confidenceScores);
        void setNegated(AssessmentSentiment assessmentSentiment, boolean isNegated);
        void setOffset(AssessmentSentiment assessmentSentiment, int offset);
        void setLength(AssessmentSentiment assessmentSentiment, int length);
    }

    /**
     * The method called from {@link AssessmentSentiment} to set it's accessor.
     *
     * @param assessmentSentimentAccessor The accessor.
     */
    public static void setAccessor(final AssessmentSentimentAccessor assessmentSentimentAccessor) {
        accessor = assessmentSentimentAccessor;
    }

    public static void setText(AssessmentSentiment assessmentSentiment, String text) {
        accessor.setText(assessmentSentiment, text);
    }

    public static void setSentiment(AssessmentSentiment assessmentSentiment, TextSentiment sentiment) {
        accessor.setSentiment(assessmentSentiment, sentiment);
    }

    public static void setConfidenceScores(AssessmentSentiment assessmentSentiment,
        SentimentConfidenceScores confidenceScores) {
        accessor.setConfidenceScores(assessmentSentiment, confidenceScores);
    }

    public static void setNegated(AssessmentSentiment assessmentSentiment, boolean isNegated) {
        accessor.setNegated(assessmentSentiment, isNegated);
    }

    public static void setOffset(AssessmentSentiment assessmentSentiment, int offset) {
        accessor.setOffset(assessmentSentiment, offset);
    }

    public static void setLength(AssessmentSentiment assessmentSentiment, int length) {
        accessor.setLength(assessmentSentiment, length);
    }
}
