// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.MinedOpinionPropertiesHelper;
import com.azure.core.util.IterableStream;

/**
 * The {@link MinedOpinion} model.
 */
public final class MinedOpinion {
    private TargetSentiment target;
    private IterableStream<AssessmentSentiment> assessments;

    static {
        MinedOpinionPropertiesHelper.setAccessor(
            new MinedOpinionPropertiesHelper.MinedOpinionAccessor() {
                @Override
                public void setTarget(MinedOpinion minedOpinion, TargetSentiment target) {
                    minedOpinion.setTarget(target);
                }

                @Override
                public void setAssessments(MinedOpinion minedOpinion, IterableStream<AssessmentSentiment> assessments) {
                    minedOpinion.setAssessments(assessments);
                }
            });
    }

    /**
     * Get the target sentiment in text, such as the attributes of products or services. For example, if a customer leaves
     * feedback about a hotel such as "the room was great, but the staff was unfriendly", opinion mining will locate
     * target sentiments in the text. The "room" and "staff" are two target sentiments recognized.
     *
     * @return The target in text.
     */
    public TargetSentiment getTarget() {
        return this.target;
    }

    /**
     * Get the assessments of target text.
     *
     * @return The assessments of target text.
     */
    public IterableStream<AssessmentSentiment> getAssessments() {
        return assessments;
    }

    private void setTarget(TargetSentiment target) {
        this.target = target;
    }

    private void setAssessments(IterableStream<AssessmentSentiment> assessments) {
        this.assessments = assessments;
    }
}
