// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;


import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * The TextSentimentResult model.
 */
@Immutable
public final class TextSentimentResult extends DocumentResult {
    private TextSentiment documentSentiment;
    private List<TextSentiment> sentenceSentiments;

    // TODO(shawn): not public modifier
    public TextSentimentResult(String id, Error error, boolean isError) {
        super(id, error, isError);
    }

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
}
