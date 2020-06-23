// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.SearchMode;

import static com.azure.search.documents.implementation.util.Constants.ENUM_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ENUM_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.SearchMode} and {@link SearchMode}.
 */
public final class SearchModeConverter {
    private static final ClientLogger LOGGER = new ClientLogger(SearchModeConverter.class);

    /**
     * Maps from enum {@link com.azure.search.documents.implementation.models.SearchMode} to enum {@link SearchMode}.
     */
    public static SearchMode map(com.azure.search.documents.implementation.models.SearchMode obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case ANY:
                return SearchMode.ANY;
            case ALL:
                return SearchMode.ALL;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_EXTERNAL_ERROR_MSG, obj)));
        }
    }

    /**
     * Maps from enum {@link SearchMode} to enum {@link com.azure.search.documents.implementation.models.SearchMode}.
     */
    public static com.azure.search.documents.implementation.models.SearchMode map(SearchMode obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case ANY:
                return com.azure.search.documents.implementation.models.SearchMode.ANY;
            case ALL:
                return com.azure.search.documents.implementation.models.SearchMode.ALL;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_INTERNAL_ERROR_MSG, obj)));
        }
    }

    private SearchModeConverter() {
    }
}
