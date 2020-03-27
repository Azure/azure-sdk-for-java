// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.exception.AzureException;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.models.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
     * @param <T> The CLR type that maps to the index schema. Instances of this type can be stored as documents
     * in the index.
     * @return A new batch containing all the actions from the given batch that failed and should be retried.
     */
    public <T> IndexBatchBase<T> findFailedActionsToRetry(IndexBatchBase<T> originBatch,
        Function<T, String> keySelector) {
        List<IndexAction<T>> failedActions = doFindFailedActionsToRetry(originBatch, keySelector);
        return new IndexBatchBase<T>().setActions(failedActions);
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
        } else if (action.getParamMap() != null) {
            return uniqueRetriableKeys.contains(keySelector.apply((T) action.getParamMap()));
        }
        return false;
    }

    private static boolean isRetriableStatusCode(int statusCode) {
        switch (statusCode) {
            case 200:
            case 201:
                return false;   // Don't retry on success.

            case 404:
            case 400:
                return false;   // Don't retry on user error.

            case 500:
                return false;   // Don't retry when something unexpected happened.

            case 422:
            case 409:
            case 503:
                return true;    // The above cases might succeed on a subsequent retry.

            default:
                // If this happens, it's a bug. Safest to assume no retry.
                return false;
        }
    }
}
