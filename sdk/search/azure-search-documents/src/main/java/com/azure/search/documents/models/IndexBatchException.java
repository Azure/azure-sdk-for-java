// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.exception.AzureException;
import com.azure.search.documents.SearchDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An {@code IndexBatchException} is thrown whenever Azure Cognitive Search index call was only partially successful.
 * Users can inspect the indexingResults to determine the operation(s) that have failed.
 */
public final class IndexBatchException extends AzureException {
    private static final long serialVersionUID = -3478124828996650248L;
    private static final String MESSAGE_FORMAT = "%s of %s indexing actions in the batch failed. The remaining"
        + " actions succeeded and modified the index. Check indexingResults for the status of each index action.";

    private final ArrayList<IndexingResult> results;

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
    public IndexBatchBase<SearchDocument> findFailedActionsToRetry(IndexBatchBase<SearchDocument> originalBatch,
        String keyFieldName) {
        return findFailedActionsToRetry(originalBatch, searchDocument -> searchDocument.get(keyFieldName).toString());
    }

    /**
     * Finds all index actions in the given batch that failed and need to be retried, and returns them in a new batch.
     *
     * @param originBatch The batch that partially failed indexing.
     * @param keySelector A lambda that retrieves a key value from a given document of type T.
     * @param <T> The given document type.
     * @return A new batch containing all the actions from the given batch that failed and should be retried.
     */
    public <T> IndexBatchBase<T> findFailedActionsToRetry(IndexBatchBase<T> originBatch,
        Function<T, String> keySelector) {
        List<IndexAction<T>> failedActions = doFindFailedActionsToRetry(originBatch, keySelector);
        return new IndexBatchBase<T>(failedActions);
    }

    /**
     * @return The indexing results returned by the service.
     */
    public List<IndexingResult> getIndexingResults() {
        return this.results;
    }

    private static String createMessage(IndexDocumentsResult result) {
        long failedResultCount = result.getResults().stream()
            .filter(r -> !r.isSucceeded())
            .count();
        return String.format(MESSAGE_FORMAT, failedResultCount, result.getResults().size());
    }

    private <T> List<IndexAction<T>> doFindFailedActionsToRetry(IndexBatchBase<T> originBatch,
        Function<T, String> keySelector) {
        Set<String> uniqueRetriableKeys = getIndexingResults().stream().filter(result ->
            isRetriableStatusCode(result.getStatusCode())).map(IndexingResult::getKey).collect(Collectors.toSet());
        return originBatch.getActions().stream().filter(action -> isActionIncluded(action,
            uniqueRetriableKeys, keySelector))
            .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private <T> boolean isActionIncluded(IndexAction<T> action, Set<String> uniqueRetriableKeys,
        Function<T, String> keySelector) {
        if (action.getDocument() != null) {
            return uniqueRetriableKeys.contains(keySelector.apply(action.getDocument()));
        }
        return false;
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
