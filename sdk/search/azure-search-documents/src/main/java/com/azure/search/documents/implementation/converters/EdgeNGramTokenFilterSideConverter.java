// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.models.EdgeNGramTokenFilterSide;

import static com.azure.search.documents.implementation.util.Constants.ENUM_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ENUM_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterSide} and
 * {@link EdgeNGramTokenFilterSide}.
 */
public final class EdgeNGramTokenFilterSideConverter {
    private static final ClientLogger LOGGER = new ClientLogger(EdgeNGramTokenFilterSideConverter.class);

    /**
     * Maps from enum {@link com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterSide} to enum
     * {@link EdgeNGramTokenFilterSide}.
     */
    public static EdgeNGramTokenFilterSide map(com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterSide obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case FRONT:
                return EdgeNGramTokenFilterSide.FRONT;
            case BACK:
                return EdgeNGramTokenFilterSide.BACK;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_EXTERNAL_ERROR_MSG, obj)));
        }
    }

    /**
     * Maps from enum {@link EdgeNGramTokenFilterSide} to enum
     * {@link com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterSide}.
     */
    public static com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterSide map(EdgeNGramTokenFilterSide obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case FRONT:
                return com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterSide.FRONT;
            case BACK:
                return com.azure.search.documents.indexes.implementation.models.EdgeNGramTokenFilterSide.BACK;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_INTERNAL_ERROR_MSG, obj)));
        }
    }

    private EdgeNGramTokenFilterSideConverter() {
    }
}
