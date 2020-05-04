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
