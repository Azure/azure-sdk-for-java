// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

public class DocumentSentiment extends DocumentResult<TextSentiment> {
    private TextSentiment documentTextSentiment;

    public TextSentiment getDocumentTextSentiment() {
        return documentTextSentiment;
    }

    public DocumentSentiment setDocumentTextSentiment(TextSentiment documentTextSentiment) {
        this.documentTextSentiment = documentTextSentiment;
        return this;
    }
}
