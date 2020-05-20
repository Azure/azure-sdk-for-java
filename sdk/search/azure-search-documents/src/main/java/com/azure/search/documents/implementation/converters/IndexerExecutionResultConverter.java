// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.models.IndexerExecutionResult;
import com.azure.search.documents.models.IndexerExecutionStatus;
import com.azure.search.documents.models.SearchIndexerError;
import com.azure.search.documents.models.SearchIndexerWarning;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.IndexerExecutionResult} and
 * {@link IndexerExecutionResult}.
 */
public final class IndexerExecutionResultConverter {
    private static final ClientLogger LOGGER = new ClientLogger(IndexerExecutionResultConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.IndexerExecutionResult} to
     * {@link IndexerExecutionResult}.
     */
    public static IndexerExecutionResult map(com.azure.search.documents.implementation.models.IndexerExecutionResult obj) {
        if (obj == null) {
            return null;
        }
        IndexerExecutionResult indexerExecutionResult = new IndexerExecutionResult();

        String _finalTrackingState = obj.getFinalTrackingState();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "finalTrackingState", _finalTrackingState);

        String _initialTrackingState = obj.getInitialTrackingState();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "initialTrackingState", _initialTrackingState);

        if (obj.getWarnings() != null) {
            List<SearchIndexerWarning> _warnings =
                obj.getWarnings().stream().map(SearchIndexerWarningConverter::map).collect(Collectors.toList());
            PrivateFieldAccessHelper.set(indexerExecutionResult, "warnings", _warnings);
        }

        String _errorMessage = obj.getErrorMessage();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "errorMessage", _errorMessage);

        OffsetDateTime _startTime = obj.getStartTime();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "startTime", _startTime);

        int _failedItemCount = obj.getFailedItemCount();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "failedItemCount", _failedItemCount);

        OffsetDateTime _endTime = obj.getEndTime();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "endTime", _endTime);

        if (obj.getErrors() != null) {
            List<SearchIndexerError> _errors =
                obj.getErrors().stream().map(SearchIndexerErrorConverter::map).collect(Collectors.toList());
            PrivateFieldAccessHelper.set(indexerExecutionResult, "errors", _errors);
        }

        if (obj.getStatus() != null) {
            IndexerExecutionStatus _status = IndexerExecutionStatusConverter.map(obj.getStatus());
            PrivateFieldAccessHelper.set(indexerExecutionResult, "status", _status);
        }

        int _itemCount = obj.getItemCount();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "itemCount", _itemCount);
        return indexerExecutionResult;
    }

    /**
     * Maps from {@link IndexerExecutionResult} to
     * {@link com.azure.search.documents.implementation.models.IndexerExecutionResult}.
     */
    public static com.azure.search.documents.implementation.models.IndexerExecutionResult map(IndexerExecutionResult obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.IndexerExecutionResult indexerExecutionResult =
            new com.azure.search.documents.implementation.models.IndexerExecutionResult();

        String _finalTrackingState = obj.getFinalTrackingState();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "finalTrackingState", _finalTrackingState);

        String _initialTrackingState = obj.getInitialTrackingState();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "initialTrackingState", _initialTrackingState);

        if (obj.getWarnings() != null) {
            List<com.azure.search.documents.implementation.models.SearchIndexerWarning> _warnings =
                obj.getWarnings().stream().map(SearchIndexerWarningConverter::map).collect(Collectors.toList());
            PrivateFieldAccessHelper.set(indexerExecutionResult, "warnings", _warnings);
        }

        String _errorMessage = obj.getErrorMessage();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "errorMessage", _errorMessage);

        OffsetDateTime _startTime = obj.getStartTime();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "startTime", _startTime);

        int _failedItemCount = obj.getFailedItemCount();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "failedItemCount", _failedItemCount);

        OffsetDateTime _endTime = obj.getEndTime();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "endTime", _endTime);

        if (obj.getErrors() != null) {
            List<com.azure.search.documents.implementation.models.SearchIndexerError> _errors =
                obj.getErrors().stream().map(SearchIndexerErrorConverter::map).collect(Collectors.toList());
            PrivateFieldAccessHelper.set(indexerExecutionResult, "errors", _errors);
        }

        if (obj.getStatus() != null) {
            com.azure.search.documents.implementation.models.IndexerExecutionStatus _status =
                IndexerExecutionStatusConverter.map(obj.getStatus());
            PrivateFieldAccessHelper.set(indexerExecutionResult, "status", _status);
        }

        int _itemCount = obj.getItemCount();
        PrivateFieldAccessHelper.set(indexerExecutionResult, "itemCount", _itemCount);
        return indexerExecutionResult;
    }
}
