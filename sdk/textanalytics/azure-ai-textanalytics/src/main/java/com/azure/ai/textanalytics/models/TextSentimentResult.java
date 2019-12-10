// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * The TextSentimentResult model.
 */
@Fluent
public final class TextSentimentResult extends DocumentResult {
    private TextSentiment documentSentiment;
    private List<TextSentiment> sentenceSentiments;

    public TextSentimentResult(String id, TextDocumentStatistics textDocumentStatistics,
                               TextSentiment documentSentiment, List<TextSentiment> sentenceSentiments) {
        super(id, textDocumentStatistics);
        this.documentSentiment = documentSentiment;
        this.sentenceSentiments = sentenceSentiments;
    }

    public TextSentiment getDocumentSentiment() {
        return documentSentiment;
    }

    public List<TextSentiment> getSentenceSentiments() {
        return sentenceSentiments;
    }

    TextSentimentResult setDocumentSentiment(TextSentiment documentSentiment) {
        this.documentSentiment = documentSentiment;
        return this;
    }

    TextSentimentResult setSentenceSentiments(List<TextSentiment> sentenceSentiments) {
        this.sentenceSentiments = sentenceSentiments;
        return this;
    }
}
