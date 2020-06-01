// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.models.IndexerStatus;

import static com.azure.search.documents.implementation.util.Constants.ENUM_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ENUM_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.IndexerStatus} and {@link IndexerStatus}.
 */
public final class IndexerStatusConverter {
    private static final ClientLogger LOGGER = new ClientLogger(IndexerStatusConverter.class);

    /**
     * Maps from enum {@link com.azure.search.documents.indexes.implementation.models.IndexerStatus} to enum
     * {@link IndexerStatus}.
     */
    public static IndexerStatus map(com.azure.search.documents.indexes.implementation.models.IndexerStatus obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case UNKNOWN:
                return IndexerStatus.UNKNOWN;
            case ERROR:
                return IndexerStatus.ERROR;
            case RUNNING:
                return IndexerStatus.RUNNING;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_EXTERNAL_ERROR_MSG, obj)));
        }
    }

    /**
     * Maps from enum {@link IndexerStatus} to enum
     * {@link com.azure.search.documents.indexes.implementation.models.IndexerStatus}.
     */
    public static com.azure.search.documents.indexes.implementation.models.IndexerStatus map(IndexerStatus obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case UNKNOWN:
                return com.azure.search.documents.indexes.implementation.models.IndexerStatus.UNKNOWN;
            case ERROR:
                return com.azure.search.documents.indexes.implementation.models.IndexerStatus.ERROR;
            case RUNNING:
                return com.azure.search.documents.indexes.implementation.models.IndexerStatus.RUNNING;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_INTERNAL_ERROR_MSG, obj)));
        }
    }

    private IndexerStatusConverter() {
    }
}
