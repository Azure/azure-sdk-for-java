// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

/**
 * The {@link SentenceSentiment} model that contains a sentiment label of a sentence, confidence score of the sentiment
 * label, length of the sentence and offset of the sentence within a document.
 */
public interface SentenceSentiment {
    /**
     * Get the text property: The sentence text.
     *
     * @return the text value.
     */
    String getText();

    /**
     * Get the grapheme length of the sentence.
     *
     * @return The grapheme length of the sentence.
     */
    int getGraphemeLength();

    /**
     * Get the grapheme offset property: start position for the sentence sentiment.
     *
     * @return The grapheme offset of sentence sentiment.
     */
    int getGraphemeOffset();

    /**
     * Get the text sentiment label: POSITIVE, NEGATIVE, or NEUTRAL.
     *
     * @return The {@link TextSentiment}.
     */
    TextSentiment getSentiment();
    /**
     * Get the confidence score of the sentiment label. All score values sum up to 1, higher the score value means
     * higher confidence the sentiment label represents.
     *
     * @return The {@link SentimentConfidenceScores}.
     */
    SentimentConfidenceScores getConfidenceScores();
}
