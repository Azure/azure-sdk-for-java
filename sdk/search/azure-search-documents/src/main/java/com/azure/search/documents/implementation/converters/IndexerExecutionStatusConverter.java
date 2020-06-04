// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.models.IndexerExecutionStatus;

import static com.azure.search.documents.implementation.util.Constants.ENUM_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ENUM_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.IndexerExecutionStatus} and
 * {@link IndexerExecutionStatus}.
 */
public final class IndexerExecutionStatusConverter {
    private static final ClientLogger LOGGER = new ClientLogger(IndexerExecutionStatusConverter.class);

    /**
     * Maps from enum {@link com.azure.search.documents.indexes.implementation.models.IndexerExecutionStatus} to enum
     * {@link IndexerExecutionStatus}.
     */
    public static IndexerExecutionStatus map(com.azure.search.documents.indexes.implementation.models.IndexerExecutionStatus obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case TRANSIENT_FAILURE:
                return IndexerExecutionStatus.TRANSIENT_FAILURE;
            case SUCCESS:
                return IndexerExecutionStatus.SUCCESS;
            case IN_PROGRESS:
                return IndexerExecutionStatus.IN_PROGRESS;
            case RESET:
                return IndexerExecutionStatus.RESET;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_EXTERNAL_ERROR_MSG, obj)));
        }
    }

    /**
     * Maps from enum {@link IndexerExecutionStatus} to enum
     * {@link com.azure.search.documents.indexes.implementation.models.IndexerExecutionStatus}.
     */
    public static com.azure.search.documents.indexes.implementation.models.IndexerExecutionStatus map(IndexerExecutionStatus obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case TRANSIENT_FAILURE:
                return com.azure.search.documents.indexes.implementation.models.IndexerExecutionStatus.TRANSIENT_FAILURE;
            case SUCCESS:
                return com.azure.search.documents.indexes.implementation.models.IndexerExecutionStatus.SUCCESS;
            case IN_PROGRESS:
                return com.azure.search.documents.indexes.implementation.models.IndexerExecutionStatus.IN_PROGRESS;
            case RESET:
                return com.azure.search.documents.indexes.implementation.models.IndexerExecutionStatus.RESET;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_INTERNAL_ERROR_MSG, obj)));
        }
    }

    private IndexerExecutionStatusConverter() {
    }
}
