// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AssessmentSentiment;
import com.azure.ai.textanalytics.models.SentenceOpinion;
import com.azure.ai.textanalytics.models.TargetSentiment;
import com.azure.core.util.IterableStream;

/**
 * The helper class to set the non-public properties of an {@link SentenceOpinion} instance.
 */
public final class SentenceOpinionPropertiesHelper {
    private static SentenceOpinionAccessor accessor;

    private SentenceOpinionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link SentenceOpinion} instance.
     */
    public interface SentenceOpinionAccessor {
        void setTarget(SentenceOpinion sentenceOpinion, TargetSentiment target);
        void setAssessments(SentenceOpinion sentenceOpinion, IterableStream<AssessmentSentiment> assessments);
    }

    /**
     * The method called from {@link SentenceOpinion} to set it's accessor.
     *
     * @param sentenceOpinionAccessor The accessor.
     */
    public static void setAccessor(final SentenceOpinionAccessor sentenceOpinionAccessor) {
        accessor = sentenceOpinionAccessor;
    }

    public static void setTarget(SentenceOpinion sentenceOpinion, TargetSentiment target) {
        accessor.setTarget(sentenceOpinion, target);
    }

    public static void setAssessments(SentenceOpinion sentenceOpinion, IterableStream<AssessmentSentiment> assessments) {
        accessor.setAssessments(sentenceOpinion, assessments);
    }
}
