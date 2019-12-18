// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 * if showStats=true was specified in the request this field will contain
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
    private final int erroneousDocumentCount;

    /*
     * Number of transactions for the request.
     */
    private final long transactionCount;

    public TextDocumentBatchStatistics(int documentCount, int validDocumentCount, int erroneousDocumentCount,
        long transactionCount) {
        this.documentCount = documentCount;
        this.validDocumentCount = validDocumentCount;
        this.erroneousDocumentCount = erroneousDocumentCount;
        this.transactionCount = transactionCount;
    }

    /**
     * Get the documentCount property: Number of documents submitted in the
     * request.
     *
     * @return the documentCount value.
     */
    public int getDocumentCount() {
        return this.documentCount;
    }

    /**
     * Get the validDocumentCount property: Number of valid documents. This
     * excludes empty, over-size limit or non-supported languages documents.
     *
     * @return the validDocumentCount value.
     */
    public int getValidDocumentCount() {
        return this.validDocumentCount;
    }

    /**
     * Get the erroneousDocumentCount property: Number of invalid documents.
     * This includes empty, over-size limit or non-supported languages
     * documents.
     *
     * @return the erroneousDocumentCount value.
     */
    public int getErroneousDocumentCount() {
        return this.erroneousDocumentCount;
    }

    /**
     * Get the transactionCount property: Number of transactions for the
     * request.
     *
     * @return the transactionCount value.
     */
    public long getTransactionCount() {
        return this.transactionCount;
    }
}
