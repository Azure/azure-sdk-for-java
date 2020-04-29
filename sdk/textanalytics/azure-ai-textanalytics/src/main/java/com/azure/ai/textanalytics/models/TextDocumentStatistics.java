// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

/**
 * If {@link TextAnalyticsRequestOptions#isIncludeStatistics()} is set to {@code true} this class will will contain
 * information about the document payload.
 */
public interface TextDocumentStatistics {
    /**
     * Get the {@code graphemeCount} property: Number of text elements recognized in the document.
     *
     * @return The {@code graphemeCount} value.
     */
    int getGraphemeCount();

    /**
     * Get the {@code transactionsCount} property: Number of transactions for the document.
     *
     * @return The {@code transactionsCount} value.
     */
    int getTransactionCount();
}
