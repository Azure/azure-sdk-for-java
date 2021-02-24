// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AssessmentSentiment;
import com.azure.ai.textanalytics.models.MinedOpinion;
import com.azure.ai.textanalytics.models.TargetSentiment;
import com.azure.core.util.IterableStream;

/**
 * The helper class to set the non-public properties of an {@link MinedOpinion} instance.
 */
public final class MinedOpinionPropertiesHelper {
    private static MinedOpinionAccessor accessor;

    private MinedOpinionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link MinedOpinion} instance.
     */
    public interface MinedOpinionAccessor {
        void setTarget(MinedOpinion minedOpinion, TargetSentiment target);
        void setAssessments(MinedOpinion minedOpinion, IterableStream<AssessmentSentiment> assessments);
    }

    /**
     * The method called from {@link MinedOpinion} to set it's accessor.
     *
     * @param minedOpinionAccessor The accessor.
     */
    public static void setAccessor(final MinedOpinionAccessor minedOpinionAccessor) {
        accessor = minedOpinionAccessor;
    }

    public static void setTarget(MinedOpinion minedOpinion, TargetSentiment target) {
        accessor.setTarget(minedOpinion, target);
    }

    public static void setAssessments(MinedOpinion minedOpinion, IterableStream<AssessmentSentiment> assessments) {
        accessor.setAssessments(minedOpinion, assessments);
    }
}
