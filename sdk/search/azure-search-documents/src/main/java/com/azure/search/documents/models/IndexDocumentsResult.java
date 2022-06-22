// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * Response containing the status of operations for all documents in the
 * indexing request.
 */
@Fluent
public final class IndexDocumentsResult {
    /*
     * The list of status information for each document in the indexing
     * request.
     */
    private final List<IndexingResult> results;

    /**
     * Constructor of {@link IndexDocumentsResult}.
     * @param results The list of status information for each document in the indexing request.
     */
    public IndexDocumentsResult(List<IndexingResult> results) {
        this.results = results;
    }

    /**
     * Get the results property: The list of status information for each
     * document in the indexing request.
     *
     * @return the results value.
     */
    public List<IndexingResult> getResults() {
        return this.results;
    }
}
