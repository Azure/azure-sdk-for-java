// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.indexes.models.IndexerExecutionResult;
import com.azure.search.documents.indexes.models.IndexerExecutionStatus;
import com.azure.search.documents.indexes.models.SearchIndexerError;
import com.azure.search.documents.indexes.models.SearchIndexerWarning;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.IndexerExecutionResult} and
 * {@link IndexerExecutionResult}.
 */
public final class IndexerExecutionResultConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.IndexerExecutionResult} to
     * {@link IndexerExecutionResult}.
     */
    public static IndexerExecutionResult map(com.azure.search.documents.indexes.implementation.models.IndexerExecutionResult obj) {
        if (obj == null) {
            return null;
        }
        IndexerExecutionResult indexerExecutionResult = new IndexerExecutionResult();

        String finalTrackingState = obj.getFinalTrackingState();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "finalTrackingState", finalTrackingState);

        String initialTrackingState = obj.getInitialTrackingState();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "initialTrackingState", initialTrackingState);

        if (obj.getWarnings() != null) {
            List<SearchIndexerWarning> warnings =
                obj.getWarnings().stream().map(SearchIndexerWarningConverter::map).collect(Collectors.toList());
            PrivateFieldAccessHelper.set(indexerExecutionResult, "warnings", warnings);
        }

        String errorMessage = obj.getErrorMessage();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "errorMessage", errorMessage);

        OffsetDateTime startTime = obj.getStartTime();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "startTime", startTime);

        int failedItemCount = obj.getFailedItemCount();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "failedItemCount", failedItemCount);

        OffsetDateTime endTime = obj.getEndTime();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "endTime", endTime);

        if (obj.getErrors() != null) {
            List<SearchIndexerError> errors =
                obj.getErrors().stream().map(SearchIndexerErrorConverter::map).collect(Collectors.toList());
            PrivateFieldAccessHelper.set(indexerExecutionResult, "errors", errors);
        }

        if (obj.getStatus() != null) {
            IndexerExecutionStatus status = IndexerExecutionStatusConverter.map(obj.getStatus());
            PrivateFieldAccessHelper.set(indexerExecutionResult, "status", status);
        }

        int itemCount = obj.getItemCount();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "itemCount", itemCount);
        return indexerExecutionResult;
    }

    /**
     * Maps from {@link IndexerExecutionResult} to
     * {@link com.azure.search.documents.indexes.implementation.models.IndexerExecutionResult}.
     */
    public static com.azure.search.documents.indexes.implementation.models.IndexerExecutionResult map(IndexerExecutionResult obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.IndexerExecutionResult indexerExecutionResult =
            new com.azure.search.documents.indexes.implementation.models.IndexerExecutionResult();

        String finalTrackingState = obj.getFinalTrackingState();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "finalTrackingState", finalTrackingState);

        String initialTrackingState = obj.getInitialTrackingState();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "initialTrackingState", initialTrackingState);

        if (obj.getWarnings() != null) {
            List<com.azure.search.documents.indexes.implementation.models.SearchIndexerWarning> warnings =
                obj.getWarnings().stream().map(SearchIndexerWarningConverter::map).collect(Collectors.toList());
            PrivateFieldAccessHelper.set(indexerExecutionResult, "warnings", warnings);
        }

        String errorMessage = obj.getErrorMessage();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "errorMessage", errorMessage);

        OffsetDateTime startTime = obj.getStartTime();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "startTime", startTime);

        int failedItemCount = obj.getFailedItemCount();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "failedItemCount", failedItemCount);

        OffsetDateTime endTime = obj.getEndTime();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "endTime", endTime);

        if (obj.getErrors() != null) {
            List<com.azure.search.documents.indexes.implementation.models.SearchIndexerError> errors =
                obj.getErrors().stream().map(SearchIndexerErrorConverter::map).collect(Collectors.toList());
            PrivateFieldAccessHelper.set(indexerExecutionResult, "errors", errors);
        }

        if (obj.getStatus() != null) {
            com.azure.search.documents.indexes.implementation.models.IndexerExecutionStatus status =
                IndexerExecutionStatusConverter.map(obj.getStatus());
            PrivateFieldAccessHelper.set(indexerExecutionResult, "status", status);
        }

        int itemCount = obj.getItemCount();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "itemCount", itemCount);
        return indexerExecutionResult;
    }

    private IndexerExecutionResultConverter() {
    }
}
