// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * if showStats=true was specified in the request this field will contain
 * information about the request payload.
 */
@Fluent
public final class TextDocumentBatchStatistics {
    /*
     * Number of documents submitted in the request.
     */
    @JsonProperty(value = "documentsCount", required = true)
    private int documentCount;

    /*
     * Number of valid documents. This excludes empty, over-size limit or
     * non-supported languages documents.
     */
    @JsonProperty(value = "validDocumentsCount", required = true)
    private int validDocumentCount;

    /*
     * Number of invalid documents. This includes empty, over-size limit or
     * non-supported languages documents.
     */
    @JsonProperty(value = "erroneousDocumentsCount", required = true)
    private int erroneousDocumentCount;

    /*
     * Number of transactions for the request.
     */
    @JsonProperty(value = "transactionsCount", required = true)
    private long transactionCount;

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
     * Set the documentCount property: Number of documents submitted in the
     * request.
     *
     * @param documentCount the documentCount value to set.
     * @return the TextBatchStatistics object itself.
     */
    public TextDocumentBatchStatistics setDocumentCount(int documentCount) {
        this.documentCount = documentCount;
        return this;
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
     * Set the validDocumentCount property: Number of valid documents. This
     * excludes empty, over-size limit or non-supported languages documents.
     *
     * @param validDocumentCount the validDocumentCount value to set.
     * @return the TextBatchStatistics object itself.
     */
    public TextDocumentBatchStatistics setValidDocumentCount(int validDocumentCount) {
        this.validDocumentCount = validDocumentCount;
        return this;
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
     * Set the erroneousDocumentCount property: Number of invalid documents.
     * This includes empty, over-size limit or non-supported languages
     * documents.
     *
     * @param erroneousDocumentCount the erroneousDocumentCount value to set.
     * @return the TextBatchStatistics object itself.
     */
    public TextDocumentBatchStatistics setErroneousDocumentCount(int erroneousDocumentCount) {
        this.erroneousDocumentCount = erroneousDocumentCount;
        return this;
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

    /**
     * Set the transactionCount property: Number of transactions for the
     * request.
     *
     * @param transactionCount the transactionCount value to set.
     * @return the TextBatchStatistics object itself.
     */
    public TextDocumentBatchStatistics setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
        return this;
    }
}
