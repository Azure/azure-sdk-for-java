// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.batching;

import com.azure.search.documents.models.IndexingResult;

import java.util.List;

/**
 * Model class which keeps track of the service results, the offset from the initial request set if it was split,
 * and whether the response is an error status.
 */
final class IndexBatchResponse {
    private final int statusCode;
    private final List<IndexingResult> results;
    private final int count;
    private final boolean isError;

    IndexBatchResponse(int statusCode, List<IndexingResult> results, int count, boolean isError) {
        this.statusCode = statusCode;
        this.results = results;
        this.count = count;
        this.isError = isError;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public List<IndexingResult> getResults() {
        return results;
    }

    public int getCount() {
        return count;
    }

    public boolean isError() {
        return isError;
    }
}
