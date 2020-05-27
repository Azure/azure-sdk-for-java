// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.AutocompleteMode;

import static com.azure.search.documents.implementation.util.Constants.ENUM_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ENUM_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.AutocompleteMode} and
 * {@link AutocompleteMode}.
 */
public final class AutocompleteModeConverter {
    private static final ClientLogger LOGGER = new ClientLogger(AutocompleteModeConverter.class);

    /**
     * Maps from enum {@link com.azure.search.documents.implementation.models.AutocompleteMode} to enum
     * {@link AutocompleteMode}.
     */
    public static AutocompleteMode map(com.azure.search.documents.implementation.models.AutocompleteMode obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case ONE_TERM:
                return AutocompleteMode.ONE_TERM;
            case TWO_TERMS:
                return AutocompleteMode.TWO_TERMS;
            case ONE_TERM_WITH_CONTEXT:
                return AutocompleteMode.ONE_TERM_WITH_CONTEXT;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_EXTERNAL_ERROR_MSG, obj)));
        }
    }

    /**
     * Maps from enum {@link AutocompleteMode} to enum
     * {@link com.azure.search.documents.implementation.models.AutocompleteMode}.
     */
    public static com.azure.search.documents.implementation.models.AutocompleteMode map(AutocompleteMode obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case ONE_TERM:
                return com.azure.search.documents.implementation.models.AutocompleteMode.ONE_TERM;
            case TWO_TERMS:
                return com.azure.search.documents.implementation.models.AutocompleteMode.TWO_TERMS;
            case ONE_TERM_WITH_CONTEXT:
                return com.azure.search.documents.implementation.models.AutocompleteMode.ONE_TERM_WITH_CONTEXT;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_INTERNAL_ERROR_MSG, obj)));
        }
    }

    private AutocompleteModeConverter() {
    }

}
