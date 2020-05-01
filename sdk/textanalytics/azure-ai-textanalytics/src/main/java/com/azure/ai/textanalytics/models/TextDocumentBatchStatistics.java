// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

/**
 * If {@link TextAnalyticsRequestOptions#isIncludeStatistics()} is set to {@code true} this class will will contain
 * information about the request payload.
 */
public interface TextDocumentBatchStatistics {
    /**
     * Get the documentCount property: Number of documents submitted in the request.
     *
     * @return The documentCount value.
     */
    int getDocumentCount();

    /**
     * Get the validDocumentCount property: Number of valid documents.
     * This excludes empty, over-size limit or non-supported languages documents.
     *
     * @return The {@code validDocumentCount} value.
     */
    int getValidDocumentCount();

    /**
     * Get the invalidDocumentCount property: Number of invalid documents.
     * This includes empty, over-size limit or non-supported languages documents.
     *
     * @return the {@code invalidDocumentCount} value.
     */
    int getInvalidDocumentCount();

    /**
     * Get the transactionCount property: Number of transactions for the request.
     *
     * @return the {@code transactionCount} value.
     */
    long getTransactionCount();
}
