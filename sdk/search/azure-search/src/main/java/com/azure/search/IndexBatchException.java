// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.exception.AzureException;
import com.azure.search.models.DocumentIndexResult;
import com.azure.search.models.IndexingResult;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@code IndexBatchException} is thrown whenever Azure Search index call was only partially successful.
 * Users can inspect the indexingResults to determine the operation(s) that have failed.
 */
public class IndexBatchException extends AzureException {
    private static final long serialVersionUID = -3478124828996650248L;
    private static final String MESSAGE_FORMAT = "%s of %s indexing actions in the batch failed. The remaining"
        + " actions succeeded and modified the index. Check indexingResults for the status of each index action.";

    private final ArrayList<IndexingResult> results;

    /**
     * Constructs an {@code IndexBatchException} from the given {@link DocumentIndexResult}.
     * @param result The DocumentIndexResult returned from the service.
     */
    IndexBatchException(DocumentIndexResult result) {
        super(createMessage(result));
        this.results = new ArrayList<>(result.getResults());
    }

    /**
     * @return The indexing results returned by the service.
     */
    public List<IndexingResult> getIndexingResults() {
        return this.results;
    }

    private static String createMessage(DocumentIndexResult result) {
        long failedResultCount = result.getResults().stream()
            .filter(r -> !r.isSucceeded())
            .count();
        return String.format(MESSAGE_FORMAT, failedResultCount, result.getResults().size());
    }
}
