// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

import java.util.List;

public class DocumentSentiment extends Sentiment {
    // if it is document sentiment level, this represents sentence sentiment, it is sentence sentiment, it will be null
    private List<Sentiment> sentenceSentiments;

    public List<Sentiment> getSentenceSentiments() {
        return sentenceSentiments;
    }

    public void setSentenceSentiments(List<Sentiment> sentenceSentiments) {
        this.sentenceSentiments = sentenceSentiments;
    }
}
