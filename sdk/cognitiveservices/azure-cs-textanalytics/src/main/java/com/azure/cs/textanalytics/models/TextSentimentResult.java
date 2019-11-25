// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.IterableStream;

import java.util.List;

/**
 * The TextSentimentResult model.
 */
@Fluent
public final class TextSentimentResult extends DocumentResult {
    private TextSentiment documentSentiment;
    private IterableStream<TextSentiment> sentenceSentiments;

    public TextSentiment getDocumentSentiment() {
        return documentSentiment;
    }

    public IterableStream<TextSentiment> getSentenceSentiments() {
        return sentenceSentiments;
    }

    TextSentimentResult setDocumentSentiment(TextSentiment documentSentiment) {
        this.documentSentiment = documentSentiment;
        return this;
    }

    TextSentimentResult setSentenceSentiments(IterableStream<TextSentiment> sentenceSentiments) {
        this.sentenceSentiments = sentenceSentiments;
        return this;
    }
}
