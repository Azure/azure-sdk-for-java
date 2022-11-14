// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.SummarySentencePropertiesHelper;
import com.azure.core.annotation.Immutable;

/**
 * The {@link SummarySentence} model.
 */
@Immutable
public final class SummarySentence {
    private String text;
    private double rankScore;
    private int length;
    private int offset;

    static {
        SummarySentencePropertiesHelper.setAccessor(
            new SummarySentencePropertiesHelper.SummarySentenceAccessor() {
                @Override
                public void setText(SummarySentence summarySentence, String text) {
                    summarySentence.setText(text);
                }

                @Override
                public void setRankScore(SummarySentence summarySentence, double rankScore) {
                    summarySentence.setRankScore(rankScore);
                }

                @Override
                public void setOffset(SummarySentence summarySentence, int offset) {
                    summarySentence.setOffset(offset);
                }

                @Override
                public void setLength(SummarySentence summarySentence, int length) {
                    summarySentence.setLength(length);
                }
            }
        );
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
