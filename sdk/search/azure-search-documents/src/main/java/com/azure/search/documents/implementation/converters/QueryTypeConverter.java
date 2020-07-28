// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.QueryType;

import static com.azure.search.documents.implementation.util.Constants.ENUM_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ENUM_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.QueryType} and {@link QueryType}.
 */
public final class QueryTypeConverter {
    private static final ClientLogger LOGGER = new ClientLogger(QueryTypeConverter.class);

    /**
     * Maps from enum {@link com.azure.search.documents.implementation.models.QueryType} to enum {@link QueryType}.
     */
    public static QueryType map(com.azure.search.documents.implementation.models.QueryType obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case SIMPLE:
                return QueryType.SIMPLE;
            case FULL:
                return QueryType.FULL;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_EXTERNAL_ERROR_MSG, obj)));
        }
    }

    /**
     * Maps from enum {@link QueryType} to enum {@link com.azure.search.documents.implementation.models.QueryType}.
     */
    public static com.azure.search.documents.implementation.models.QueryType map(QueryType obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case SIMPLE:
                return com.azure.search.documents.implementation.models.QueryType.SIMPLE;
            case FULL:
                return com.azure.search.documents.implementation.models.QueryType.FULL;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_INTERNAL_ERROR_MSG, obj)));
        }
    }

    private QueryTypeConverter() {
    }
}
