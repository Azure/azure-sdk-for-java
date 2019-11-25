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
    private TextSentiment textSentiment;
    private IterableStream<TextSentiment> items;

    public TextSentiment getTextSentiment() {
        return textSentiment;
    }

    TextSentimentResult setTextSentiment(TextSentiment textSentiment) {
        this.textSentiment = textSentiment;
        return this;
    }

    public IterableStream<TextSentiment> getItems() {
        return items;
    }

    TextSentimentResult setItems(IterableStream<TextSentiment> items) {
        this.items = items;
        return this;
    }
}
