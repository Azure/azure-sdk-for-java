// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

/**
 * The {@link AnalyzeSentimentResult} model.
 */
public interface AnalyzeSentimentResult extends DocumentResult {
    /**
     * Get the document sentiment.
     *
     * @return The document sentiment.
     */
    DocumentSentiment getDocumentSentiment();
}
