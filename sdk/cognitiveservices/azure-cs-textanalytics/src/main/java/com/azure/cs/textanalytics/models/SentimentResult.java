// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

public class SentimentResult {

    private Sentiment sentiment;

    private DocumentResult<Sentiment> sentenceSentiments;

    public Sentiment getSentiment() {
        return sentiment;
    }

    public void setSentiment(Sentiment sentiment) {
        this.sentiment = sentiment;
    }

    public DocumentResult<Sentiment> getSentenceSentiments() {
        return sentenceSentiments;
    }

    public void setSentenceSentiments(DocumentResult<Sentiment> sentenceSentiments) {
        this.sentenceSentiments = sentenceSentiments;
    }
}
