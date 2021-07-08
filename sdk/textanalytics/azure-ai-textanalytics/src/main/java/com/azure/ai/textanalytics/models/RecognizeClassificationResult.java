// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 *
 */
@Immutable
public final class RecognizeClassificationResult extends TextAnalyticsResult {
    private DocumentClassification documentClassification;

    /**
     * Creates a {@link RecognizeClassificationResult} model.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     */
    public RecognizeClassificationResult(String id, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error) {
        super(id, textDocumentStatistics, error);
    }

    /**
     *
     * @return
     */
    public DocumentClassification getDocumentClassification() {
        throwExceptionIfError();
        return documentClassification;
    }
}
