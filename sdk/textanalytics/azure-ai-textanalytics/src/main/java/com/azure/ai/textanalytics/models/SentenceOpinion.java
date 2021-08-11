// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.SentenceOpinionPropertiesHelper;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link SentenceOpinion} model.
 */
@Immutable
public final class SentenceOpinion {
    private TargetSentiment target;
    private IterableStream<AssessmentSentiment> assessments;

    static {
        SentenceOpinionPropertiesHelper.setAccessor(
            new SentenceOpinionPropertiesHelper.SentenceOpinionAccessor() {
                @Override
                public void setTarget(SentenceOpinion sentenceOpinion, TargetSentiment target) {
                    sentenceOpinion.setTarget(target);
                }

                @Override
                public void setAssessments(SentenceOpinion sentenceOpinion, IterableStream<AssessmentSentiment> assessments) {
                    sentenceOpinion.setAssessments(assessments);
                }
            });
    }

    /**
     * Gets the target sentiment in text, such as the attributes of products or services. For example, if a customer leaves
     * feedback about a hotel such as "the room was great, but the staff was unfriendly", opinion mining will locate
     * target sentiments in the text. The "room" and "staff" are two target sentiments recognized.
     *
     * @return The target in text.
     */
    public TargetSentiment getTarget() {
        return this.target;
    }

    /**
     * Gets the assessments of target text.
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
