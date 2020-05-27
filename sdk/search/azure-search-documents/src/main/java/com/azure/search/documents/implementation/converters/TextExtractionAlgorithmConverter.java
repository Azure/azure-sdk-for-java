// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.models.TextExtractionAlgorithm;

import static com.azure.search.documents.implementation.util.Constants.ENUM_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ENUM_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.TextExtractionAlgorithm} and
 * {@link TextExtractionAlgorithm}.
 */
public final class TextExtractionAlgorithmConverter {
    private static final ClientLogger LOGGER = new ClientLogger(TextExtractionAlgorithmConverter.class);

    /**
     * Maps from enum {@link com.azure.search.documents.indexes.implementation.models.TextExtractionAlgorithm} to enum
     * {@link TextExtractionAlgorithm}.
     */
    public static TextExtractionAlgorithm map(com.azure.search.documents.indexes.implementation.models.TextExtractionAlgorithm obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case PRINTED:
                return TextExtractionAlgorithm.PRINTED;
            case HANDWRITTEN:
                return TextExtractionAlgorithm.HANDWRITTEN;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_EXTERNAL_ERROR_MSG, obj)));
        }
    }

    /**
     * Maps from enum {@link TextExtractionAlgorithm} to enum
     * {@link com.azure.search.documents.indexes.implementation.models.TextExtractionAlgorithm}.
     */
    public static com.azure.search.documents.indexes.implementation.models.TextExtractionAlgorithm map(TextExtractionAlgorithm obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case PRINTED:
                return com.azure.search.documents.indexes.implementation.models.TextExtractionAlgorithm.PRINTED;
            case HANDWRITTEN:
                return com.azure.search.documents.indexes.implementation.models.TextExtractionAlgorithm.HANDWRITTEN;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_INTERNAL_ERROR_MSG, obj)));
        }
    }

    private TextExtractionAlgorithmConverter() {
    }
}
