// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.SentenceSentimentPropertiesHelper;
import com.azure.core.util.IterableStream;

/**
 * The {@link SentenceSentiment} model that contains a sentiment label of a sentence, confidence scores of the
 * sentiment label, sentence opinions, and offset of sentence within a document.
 */
public final class SentenceSentiment {
    private final String text;
    private final TextSentiment sentiment;
    private final SentimentConfidenceScores confidenceScores;
    private IterableStream<SentenceOpinion> opinions;
    private int offset;
    private int length;

    /**
     * Creates a {@link SentenceSentiment} model that describes the sentiment analysis of sentence.
     *
     * @param text The sentence text.
     * @param sentiment The sentiment label of the sentence.
     * @param confidenceScores The sentiment confidence score (Softmax score) between 0 and 1, for each sentiment label.
     * Higher values signify higher confidence.
     */
    public SentenceSentiment(String text, TextSentiment sentiment, SentimentConfidenceScores confidenceScores) {
        this.text = text;
        this.sentiment = sentiment;
        this.confidenceScores = confidenceScores;
    }

    static {
        SentenceSentimentPropertiesHelper.setAccessor(
            new SentenceSentimentPropertiesHelper.SentenceSentimentAccessor() {
                @Override
                public void setOpinions(SentenceSentiment sentenceSentiment, IterableStream<SentenceOpinion> opinions) {
                    sentenceSentiment.setOpinions(opinions);
                }

                @Override
                public void setOffset(SentenceSentiment sentenceSentiment, int offset) {
                    sentenceSentiment.setOffset(offset);
                }

                @Override
                public void setLength(SentenceSentiment sentenceSentiment, int length) {
                    sentenceSentiment.setLength(length);
                }
            });
    }

    /**
     * Gets the sentence text property.
     *
     * @return The text property value.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Gets the text sentiment label: POSITIVE, NEGATIVE, or NEUTRAL.
     *
     * @return The {@link TextSentiment}.
     */
    public TextSentiment getSentiment() {
        return sentiment;
    }

    /**
     * Gets the confidence score of the sentiment label. All score values sum up to 1, the higher the score, the
     * higher the confidence in the sentiment.
     *
     * @return The {@link SentimentConfidenceScores}.
     */
    public SentimentConfidenceScores getConfidenceScores() {
        return confidenceScores;
    }

    /**
     * Gets the sentence opinions of sentence sentiment.
     * This is only returned if you pass the opinion mining parameter to the analyze sentiment APIs.
     *
     * @return The sentence opinions of sentence sentiment.
     */
    public IterableStream<SentenceOpinion> getOpinions() {
        return opinions;
    }

    /**
     * Gets the offset of sentence. The start position for the sentence in a document.
     *
     * @return The offset of sentence.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Gets the length of sentence.
     *
     * @return The length of sentence.
     */
    public int getLength() {
        return length;
    }

    private void setOpinions(IterableStream<SentenceOpinion> opinions) {
        this.opinions = opinions;
    }

    private void setOffset(int offset) {
        this.offset = offset;
    }

    private void setLength(int length) {
        this.length = length;
    }
}
