// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * The TextSentimentResult model.
 */
// TODO (shawn): Should be @Immutable, but will produce spotbug/checkstyle error
@Fluent
public final class TextSentimentResult extends DocumentResult {
    private final TextSentiment documentSentiment;
    private final List<TextSentiment> sentenceSentiments;

    public TextSentimentResult(String id, TextDocumentStatistics textDocumentStatistics, Error error,
        TextSentiment documentSentiment, List<TextSentiment> sentenceSentiments) {
        super(id, textDocumentStatistics, error);
        this.documentSentiment = documentSentiment;
        this.sentenceSentiments = sentenceSentiments;
    }

    public TextSentiment getDocumentSentiment() {
        return documentSentiment;
    }

    public List<TextSentiment> getSentenceSentiments() {
        return sentenceSentiments;
    }
}
