// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.TextSplitMode;

import static com.azure.search.documents.implementation.util.Constants.ENUM_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ENUM_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.TextSplitMode} and {@link TextSplitMode}.
 */
public final class TextSplitModeConverter {
    private static final ClientLogger LOGGER = new ClientLogger(TextSplitModeConverter.class);

    /**
     * Maps from enum {@link com.azure.search.documents.implementation.models.TextSplitMode} to enum
     * {@link TextSplitMode}.
     */
    public static TextSplitMode map(com.azure.search.documents.implementation.models.TextSplitMode obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case PAGES:
                return TextSplitMode.PAGES;
            case SENTENCES:
                return TextSplitMode.SENTENCES;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_EXTERNAL_ERROR_MSG, obj)));
        }
    }

    /**
     * Maps from enum {@link TextSplitMode} to enum
     * {@link com.azure.search.documents.implementation.models.TextSplitMode}.
     */
    public static com.azure.search.documents.implementation.models.TextSplitMode map(TextSplitMode obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case PAGES:
                return com.azure.search.documents.implementation.models.TextSplitMode.PAGES;
            case SENTENCES:
                return com.azure.search.documents.implementation.models.TextSplitMode.SENTENCES;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_INTERNAL_ERROR_MSG, obj)));
        }
    }
}
