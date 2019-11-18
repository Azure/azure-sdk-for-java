// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

import java.util.List;

public class DocumentSentiment extends DocumentResult<Sentiment> {
    private Sentiment documentSentiment;

    public Sentiment getDocumentSentiment() {
        return documentSentiment;
    }

    public DocumentSentiment setDocumentSentiment(Sentiment documentSentiment) {
        this.documentSentiment = documentSentiment;
        return this;
    }
}
