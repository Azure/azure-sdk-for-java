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
    private IterableStream<TextSentiment> textSentiments;

    public TextSentiment getDocumentSentiment() {
        return documentSentiment;
    }

    public IterableStream<TextSentiment> getTextSentiments() {
        return textSentiments;
    }

    TextSentimentResult setDocumentSentiment(TextSentiment documentSentiment) {
        this.documentSentiment = documentSentiment;
        return this;
    }

    TextSentimentResult setTextSentiments(IterableStream<TextSentiment> textSentiments) {
        this.textSentiments = textSentiments;
        return this;
    }
}
