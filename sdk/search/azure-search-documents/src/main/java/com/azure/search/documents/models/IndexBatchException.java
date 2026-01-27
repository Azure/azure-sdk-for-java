// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.exception.AzureException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An {@code IndexBatchException} is thrown whenever Azure AI Search index call was only partially successful.
 * Users can inspect the indexingResults to determine the operation(s) that have failed.
 */
public final class IndexBatchException extends AzureException {
    private static final long serialVersionUID = -3478124828996650248L;
    private static final String MESSAGE_FORMAT = "%s of %s indexing actions in the batch failed. The remaining"
        + " actions succeeded and modified the index. Check indexingResults for the status of each index action.";

    /**
     * Indexing results.
     */
    private final List<IndexingResult> results;

    /**
     * Constructs an {@code IndexBatchException} from the given {@link IndexDocumentsResult}.
     *
     * @param result The DocumentIndexResult returned from the service.
     */
    public IndexBatchException(IndexDocumentsResult result) {
        super(createMessage(result));
        this.results = new ArrayList<>(result.getResults());
    }

    /**
     * Finds all index actions in the given batch that failed and need to be retried, and returns them in a new batch.
     *
     * @param originalBatch The batch that partially failed indexing.
     * @param keyFieldName The name of the key field from the index schema.
     * @return A new batch containing all the actions from the given batch that failed and should be retried.
     */
    public IndexDocumentsBatch findFailedActionsToRetry(IndexDocumentsBatch originalBatch, String keyFieldName) {
        Set<String> uniqueRetriableKeys = getIndexingResults().stream()
            .filter(result -> isRetriableStatusCode(result.getStatusCode()))
            .map(IndexingResult::getKey)
            .collect(Collectors.toSet());
        return new IndexDocumentsBatch(originalBatch.getActions()
            .stream()
            .filter(action -> isActionIncluded(action, uniqueRetriableKeys, keyFieldName))
            .collect(Collectors.toList()));
    }

    /**
     * Gets the indexing results returned by the service.
     *
     * @return The indexing results returned by the service.
     */
    public List<IndexingResult> getIndexingResults() {
        return this.results;
    }

    private static String createMessage(IndexDocumentsResult result) {
        long failedResultCount = result.getResults().stream().filter(r -> !r.isSucceeded()).count();
        return String.format(MESSAGE_FORMAT, failedResultCount, result.getResults().size());
    }

    private static boolean isActionIncluded(IndexAction action, Set<String> uniqueRetriableKeys, String keyFieldName) {
        return action.getAdditionalProperties() != null
            && uniqueRetriableKeys.contains(Objects.toString(action.getAdditionalProperties().get(keyFieldName), null));
    }

    /**
     * Checks whether status code is retriable or not.
     * <ul>
     * <li>'409': A version conflict was detected when attempting to index a document.</li>
     * <li>'422': The index is temporarily unavailable because it was updated with the
     * 'allowIndexDowntime' flag set to 'true'.</li>
     * <li>'503': Your search service is temporarily unavailable, possibly due to heavy load.</li>
     * </ul>
     *
     * @param statusCode The status code from http response.
     * @return Indicates whether it is retriable or not.
     */
    private static boolean isRetriableStatusCode(int statusCode) {
        // 503 Service Unavailable:
        // server error response code indicates that the server is not ready to handle the request
        return statusCode == 409 || statusCode == 422 || statusCode == 503;
    }
}
