// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 * If {@link TextAnalyticsRequestOptions#showStatistics()} is set to {@code true} this class will will contain
 * information about the document payload.
 */
@Immutable
public final class TextDocumentStatistics {
    /*
     * Number of text elements recognized in the document.
     */
    private final int characterCount;

    /*
     * Number of transactions for the document.
     */
    private final int transactionCount;

    /**
     * Creates a {@code TextDocumentStatistics} model that describes the statistics of text document.
     *
     * @param characterCount number of text elements recognized in the document
     * @param transactionCount number of transactions for the document
     */
    public TextDocumentStatistics(int characterCount, int transactionCount) {
        this.characterCount = characterCount;
        this.transactionCount = transactionCount;
    }

    /**
     * Get the charactersCount property: Number of text elements recognized in the document.
     *
     * @return the charactersCount value.
     */
    public int getCharacterCount() {
        return this.characterCount;
    }

    /**
     * Get the transactionsCount property: Number of transactions for the document.
     *
     * @return the transactionsCount value.
     */
    public int getTransactionCount() {
        return this.transactionCount;
    }
}
