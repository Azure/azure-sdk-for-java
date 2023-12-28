// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 * If {@link TextAnalyticsRequestOptions#isIncludeStatistics()} is set to {@code true} this class will will contain
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
     * @param documentCount The number of documents submitted in the request.
     * @param validDocumentCount The number of valid documents. This excludes empty, over-size limit or
     * non-supported languages documents.
     * @param invalidDocumentCount The number of invalid documents. This includes empty, over-size limit or
     * non-supported languages documents.
     * @param transactionCount The number of transactions for the request.
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
     * @return The documentCount value.
     */
    public int getDocumentCount() {
        return this.documentCount;
    }

    /**
     * Get the validDocumentCount property: Number of valid documents.
     * This excludes empty, over-size limit or non-supported languages documents.
     *
     * @return The {@code validDocumentCount} value.
     */
    public int getValidDocumentCount() {
        return this.validDocumentCount;
    }

    /**
     * Get the invalidDocumentCount property: Number of invalid documents.
     * This includes empty, over-size limit or non-supported languages documents.
     *
     * @return the {@code invalidDocumentCount} value.
     */
    public int getInvalidDocumentCount() {
        return this.invalidDocumentCount;
    }

    /**
     * Get the transactionCount property: Number of transactions for the request.
     *
     * @return the {@code transactionCount} value.
     */
    public long getTransactionCount() {
        return this.transactionCount;
    }
}
