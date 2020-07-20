// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link SentenceSentiment} model that contains a sentiment label of a sentence, confidence score of the
 * sentiment label, length of the sentence and offset of the sentence within a document.
 */
@Immutable
public final class SentenceSentiment {
    private final String text;
    private final SentimentConfidenceScores confidenceScores;
    private final TextSentiment sentiment;
    private final IterableStream<AspectSentiment> aspects;

    /**
     * Creates a {@link SentenceSentiment} model that describes the sentiment analysis of sentence.
     *
     * @param text The sentence text.
     * @param sentiment The sentiment label of the sentence.
     * @param confidenceScores The sentiment confidence score (Softmax score) between 0 and 1, for each sentiment label.
     *   Higher values signify higher confidence.
     * @param aspects The aspect of the sentence sentiment.
     */
    public SentenceSentiment(String text, TextSentiment sentiment, SentimentConfidenceScores confidenceScores,
        IterableStream<AspectSentiment> aspects) {
        this.text = text;
        this.sentiment = sentiment;
        this.confidenceScores = confidenceScores;
        this.aspects = aspects;
    }

    /**
     * Get the sentence text property.
     *
     * @return the text property value.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Get the text sentiment label: POSITIVE, NEGATIVE, or NEUTRAL.
     *
     * @return The {@link TextSentiment}.
     */
    public TextSentiment getSentiment() {
        return sentiment;
    }

    /**
     * Get the confidence score of the sentiment label. All score values sum up to 1, higher the score value means
     * higher confidence the sentiment label represents.
     *
     * @return The {@link SentimentConfidenceScores}.
     */
    public SentimentConfidenceScores getConfidenceScores() {
        return confidenceScores;
    }

    /**
     * Get the aspects of sentence sentiment.
     *
     * @return The aspects of sentence sentiment.
     */
    public IterableStream<AspectSentiment> getAspects() {
        return aspects;
    }
}
