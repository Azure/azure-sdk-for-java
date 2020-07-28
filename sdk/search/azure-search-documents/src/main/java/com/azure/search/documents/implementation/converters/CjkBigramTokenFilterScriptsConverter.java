// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.models.CjkBigramTokenFilterScripts;

import static com.azure.search.documents.implementation.util.Constants.ENUM_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ENUM_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.CjkBigramTokenFilterScripts} and
 * {@link CjkBigramTokenFilterScripts}.
 */
public final class CjkBigramTokenFilterScriptsConverter {
    private static final ClientLogger LOGGER = new ClientLogger(CjkBigramTokenFilterScriptsConverter.class);

    /**
     * Maps from enum {@link com.azure.search.documents.indexes.implementation.models.CjkBigramTokenFilterScripts} to enum
     * {@link CjkBigramTokenFilterScripts}.
     */
    public static CjkBigramTokenFilterScripts map(com.azure.search.documents.indexes.implementation.models.CjkBigramTokenFilterScripts obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case HAN:
                return CjkBigramTokenFilterScripts.HAN;
            case HIRAGANA:
                return CjkBigramTokenFilterScripts.HIRAGANA;
            case KATAKANA:
                return CjkBigramTokenFilterScripts.KATAKANA;
            case HANGUL:
                return CjkBigramTokenFilterScripts.HANGUL;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_EXTERNAL_ERROR_MSG, obj)));
        }
    }

    /**
     * Maps from enum {@link CjkBigramTokenFilterScripts} to enum
     * {@link com.azure.search.documents.indexes.implementation.models.CjkBigramTokenFilterScripts}.
     */
    public static com.azure.search.documents.indexes.implementation.models.CjkBigramTokenFilterScripts map(CjkBigramTokenFilterScripts obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case HAN:
                return com.azure.search.documents.indexes.implementation.models.CjkBigramTokenFilterScripts.HAN;
            case HIRAGANA:
                return com.azure.search.documents.indexes.implementation.models.CjkBigramTokenFilterScripts.HIRAGANA;
            case KATAKANA:
                return com.azure.search.documents.indexes.implementation.models.CjkBigramTokenFilterScripts.KATAKANA;
            case HANGUL:
                return com.azure.search.documents.indexes.implementation.models.CjkBigramTokenFilterScripts.HANGUL;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_INTERNAL_ERROR_MSG, obj)));
        }
    }

    private CjkBigramTokenFilterScriptsConverter() {
    }
}
