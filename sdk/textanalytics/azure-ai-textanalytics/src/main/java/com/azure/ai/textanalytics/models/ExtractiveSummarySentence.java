// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.ExtractiveSummarySentencePropertiesHelper;
import com.azure.core.annotation.Immutable;

/**
 * The {@code ExtractiveSummarySentence} model.
 */
@Immutable
public final class ExtractiveSummarySentence {
    private String text;
    private double rankScore;
    private int length;
    private int offset;

    static {
        ExtractiveSummarySentencePropertiesHelper.setAccessor(
            new ExtractiveSummarySentencePropertiesHelper.ExtractiveSummarySentenceAccessor() {
                @Override
                public void setText(ExtractiveSummarySentence extractiveSummarySentence, String text) {
                    extractiveSummarySentence.setText(text);
                }

                @Override
                public void setRankScore(ExtractiveSummarySentence extractiveSummarySentence, double rankScore) {
                    extractiveSummarySentence.setRankScore(rankScore);
                }

                @Override
                public void setOffset(ExtractiveSummarySentence extractiveSummarySentence, int offset) {
                    extractiveSummarySentence.setOffset(offset);
                }

                @Override
                public void setLength(ExtractiveSummarySentence extractiveSummarySentence, int length) {
                    extractiveSummarySentence.setLength(length);
                }
            }
        );
    }

    /**
     * Constructs a {@code ExtractiveSummarySentence} model.
     */
    public ExtractiveSummarySentence() {
    }

    /**
     * Gets the text property: extractive summarization sentence text.
     *
     * @return The {@code text} value.
     */
    public String getText() {
        return text;
    }

    /**
     * Gets the rank score of the extractive text summarization. Higher the score, higher importance of the sentence.
     *
     * @return The rank score value.
     */
    public double getRankScore() {
        return rankScore;
    }

    /**
     * Gets the length of sentence.
     *
     * @return The length of sentence.
     */
    public int getLength() {
        return length;
    }

    /**
     * Gets the offset of sentence.
     *
     * @return The offset of sentence.
     */
    public int getOffset() {
        return offset;
    }

    private void setText(String text) {
        this.text = text;
    }

    private void setRankScore(double rankScore) {
        this.rankScore = rankScore;
    }

    private void setOffset(int offset) {
        this.offset = offset;
    }

    private void setLength(int length) {
        this.length = length;
    }
}
