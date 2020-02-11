// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 * If {@link TextAnalyticsRequestOptions#showStatistics()} is set to {@code true} this class will will contain
 * information about the request payload.
 */
@Immutable
public final class TextDocumentBatchStatistics {
    /*
     * Number of documents submitted in the request.
     */
    private final int documentCount;

    /*
     * Number of valid documents. This excludes empty, over-size limit or
     * non-supported languages documents.
     */
    private final int validDocumentCount;

    /*
     * Number of invalid documents. This includes empty, over-size limit or
     * non-supported languages documents.
     */
    private final int invalidDocumentCount;

    /*
     * Number of transactions for the request.
     */
    private final long transactionCount;

    /**
     * Creates a {@code TextDocumentBatchStatistics} model that describes the statistics of batch text.
     *
     * @param documentCount number of documents submitted in the request
     * @param validDocumentCount number of valid documents. This excludes empty, over-size limit or
     * non-supported languages documents
     * @param invalidDocumentCount Number of invalid documents. This includes empty, over-size limit or
     * non-supported languages documents
     * @param transactionCount number of transactions for the request
     */
    public TextDocumentBatchStatistics(int documentCount, int validDocumentCount, int invalidDocumentCount,
        long transactionCount) {
        this.documentCount = documentCount;
        this.validDocumentCount = validDocumentCount;
        this.invalidDocumentCount = invalidDocumentCount;
        this.transactionCount = transactionCount;
    }

    /**
     * Get the documentCount property: Number of documents submitted in the request.
     *
     * @return the documentCount value.
     */
    public int getDocumentCount() {
        return this.documentCount;
    }

    /**
     * Get the validDocumentCount property: Number of valid documents.
     * This excludes empty, over-size limit or non-supported languages documents.
     *
     * @return the validDocumentCount value.
     */
    public int getValidDocumentCount() {
        return this.validDocumentCount;
    }

    /**
     * Get the invalidDocumentCount property: Number of invalid documents.
     * This includes empty, over-size limit or non-supported languages documents.
     *
     * @return the invalidDocumentCount value.
     */
    public int getInvalidDocumentCount() {
        return this.invalidDocumentCount;
    }

    /**
     * Get the transactionCount property: Number of transactions for the request.
     *
     * @return the transactionCount value.
     */
    public long getTransactionCount() {
        return this.transactionCount;
    }
}
