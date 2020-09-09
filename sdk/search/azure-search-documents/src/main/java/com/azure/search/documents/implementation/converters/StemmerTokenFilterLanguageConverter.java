// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.models.StemmerTokenFilterLanguage;

import static com.azure.search.documents.implementation.util.Constants.ENUM_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ENUM_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage} and
 * {@link StemmerTokenFilterLanguage}.
 */
public final class StemmerTokenFilterLanguageConverter {
    private static final ClientLogger LOGGER = new ClientLogger(StemmerTokenFilterLanguageConverter.class);

    /**
     * Maps from enum {@link com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage} to enum
     * {@link StemmerTokenFilterLanguage}.
     */
    public static StemmerTokenFilterLanguage map(com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case ARABIC:
                return StemmerTokenFilterLanguage.ARABIC;
            case ARMENIAN:
                return StemmerTokenFilterLanguage.ARMENIAN;
            case BASQUE:
                return StemmerTokenFilterLanguage.BASQUE;
            case BRAZILIAN:
                return StemmerTokenFilterLanguage.BRAZILIAN;
            case BULGARIAN:
                return StemmerTokenFilterLanguage.BULGARIAN;
            case CATALAN:
                return StemmerTokenFilterLanguage.CATALAN;
            case CZECH:
                return StemmerTokenFilterLanguage.CZECH;
            case DANISH:
                return StemmerTokenFilterLanguage.DANISH;
            case DUTCH:
                return StemmerTokenFilterLanguage.DUTCH;
            case DUTCH_KP:
                return StemmerTokenFilterLanguage.DUTCH_KP;
            case ENGLISH:
                return StemmerTokenFilterLanguage.ENGLISH;
            case LIGHT_ENGLISH:
                return StemmerTokenFilterLanguage.LIGHT_ENGLISH;
            case MINIMAL_ENGLISH:
                return StemmerTokenFilterLanguage.MINIMAL_ENGLISH;
            case POSSESSIVE_ENGLISH:
                return StemmerTokenFilterLanguage.POSSESSIVE_ENGLISH;
            case PORTER2:
                return StemmerTokenFilterLanguage.PORTER2;
            case LOVINS:
                return StemmerTokenFilterLanguage.LOVINS;
            case FINNISH:
                return StemmerTokenFilterLanguage.FINNISH;
            case LIGHT_FINNISH:
                return StemmerTokenFilterLanguage.LIGHT_FINNISH;
            case FRENCH:
                return StemmerTokenFilterLanguage.FRENCH;
            case LIGHT_FRENCH:
                return StemmerTokenFilterLanguage.LIGHT_FRENCH;
            case MINIMAL_FRENCH:
                return StemmerTokenFilterLanguage.MINIMAL_FRENCH;
            case GALICIAN:
                return StemmerTokenFilterLanguage.GALICIAN;
            case MINIMAL_GALICIAN:
                return StemmerTokenFilterLanguage.MINIMAL_GALICIAN;
            case GERMAN:
                return StemmerTokenFilterLanguage.GERMAN;
            case GERMAN2:
                return StemmerTokenFilterLanguage.GERMAN2;
            case LIGHT_GERMAN:
                return StemmerTokenFilterLanguage.LIGHT_GERMAN;
            case MINIMAL_GERMAN:
                return StemmerTokenFilterLanguage.MINIMAL_GERMAN;
            case GREEK:
                return StemmerTokenFilterLanguage.GREEK;
            case HINDI:
                return StemmerTokenFilterLanguage.HINDI;
            case HUNGARIAN:
                return StemmerTokenFilterLanguage.HUNGARIAN;
            case LIGHT_HUNGARIAN:
                return StemmerTokenFilterLanguage.LIGHT_HUNGARIAN;
            case INDONESIAN:
                return StemmerTokenFilterLanguage.INDONESIAN;
            case IRISH:
                return StemmerTokenFilterLanguage.IRISH;
            case ITALIAN:
                return StemmerTokenFilterLanguage.ITALIAN;
            case LIGHT_ITALIAN:
                return StemmerTokenFilterLanguage.LIGHT_ITALIAN;
            case SORANI:
                return StemmerTokenFilterLanguage.SORANI;
            case LATVIAN:
                return StemmerTokenFilterLanguage.LATVIAN;
            case NORWEGIAN:
                return StemmerTokenFilterLanguage.NORWEGIAN;
            case LIGHT_NORWEGIAN:
                return StemmerTokenFilterLanguage.LIGHT_NORWEGIAN;
            case MINIMAL_NORWEGIAN:
                return StemmerTokenFilterLanguage.MINIMAL_NORWEGIAN;
            case LIGHT_NYNORSK:
                return StemmerTokenFilterLanguage.LIGHT_NYNORSK;
            case MINIMAL_NYNORSK:
                return StemmerTokenFilterLanguage.MINIMAL_NYNORSK;
            case PORTUGUESE:
                return StemmerTokenFilterLanguage.PORTUGUESE;
            case LIGHT_PORTUGUESE:
                return StemmerTokenFilterLanguage.LIGHT_PORTUGUESE;
            case MINIMAL_PORTUGUESE:
                return StemmerTokenFilterLanguage.MINIMAL_PORTUGUESE;
            case PORTUGUESE_RSLP:
                return StemmerTokenFilterLanguage.PORTUGUESE_RSLP;
            case ROMANIAN:
                return StemmerTokenFilterLanguage.ROMANIAN;
            case RUSSIAN:
                return StemmerTokenFilterLanguage.RUSSIAN;
            case LIGHT_RUSSIAN:
                return StemmerTokenFilterLanguage.LIGHT_RUSSIAN;
            case SPANISH:
                return StemmerTokenFilterLanguage.SPANISH;
            case LIGHT_SPANISH:
                return StemmerTokenFilterLanguage.LIGHT_SPANISH;
            case SWEDISH:
                return StemmerTokenFilterLanguage.SWEDISH;
            case LIGHT_SWEDISH:
                return StemmerTokenFilterLanguage.LIGHT_SWEDISH;
            case TURKISH:
                return StemmerTokenFilterLanguage.TURKISH;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_EXTERNAL_ERROR_MSG, obj)));
        }
    }

    /**
     * Maps from enum {@link StemmerTokenFilterLanguage} to enum
     * {@link com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage}.
     */
    public static com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage map(StemmerTokenFilterLanguage obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case ARABIC:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.ARABIC;
            case ARMENIAN:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.ARMENIAN;
            case BASQUE:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.BASQUE;
            case BRAZILIAN:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.BRAZILIAN;
            case BULGARIAN:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.BULGARIAN;
            case CATALAN:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.CATALAN;
            case CZECH:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.CZECH;
            case DANISH:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.DANISH;
            case DUTCH:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.DUTCH;
            case DUTCH_KP:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.DUTCH_KP;
            case ENGLISH:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.ENGLISH;
            case LIGHT_ENGLISH:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.LIGHT_ENGLISH;
            case MINIMAL_ENGLISH:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.MINIMAL_ENGLISH;
            case POSSESSIVE_ENGLISH:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.POSSESSIVE_ENGLISH;
            case PORTER2:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.PORTER2;
            case LOVINS:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.LOVINS;
            case FINNISH:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.FINNISH;
            case LIGHT_FINNISH:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.LIGHT_FINNISH;
            case FRENCH:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.FRENCH;
            case LIGHT_FRENCH:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.LIGHT_FRENCH;
            case MINIMAL_FRENCH:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.MINIMAL_FRENCH;
            case GALICIAN:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.GALICIAN;
            case MINIMAL_GALICIAN:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.MINIMAL_GALICIAN;
            case GERMAN:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.GERMAN;
            case GERMAN2:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.GERMAN2;
            case LIGHT_GERMAN:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.LIGHT_GERMAN;
            case MINIMAL_GERMAN:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.MINIMAL_GERMAN;
            case GREEK:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.GREEK;
            case HINDI:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.HINDI;
            case HUNGARIAN:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.HUNGARIAN;
            case LIGHT_HUNGARIAN:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.LIGHT_HUNGARIAN;
            case INDONESIAN:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.INDONESIAN;
            case IRISH:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.IRISH;
            case ITALIAN:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.ITALIAN;
            case LIGHT_ITALIAN:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.LIGHT_ITALIAN;
            case SORANI:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.SORANI;
            case LATVIAN:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.LATVIAN;
            case NORWEGIAN:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.NORWEGIAN;
            case LIGHT_NORWEGIAN:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.LIGHT_NORWEGIAN;
            case MINIMAL_NORWEGIAN:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.MINIMAL_NORWEGIAN;
            case LIGHT_NYNORSK:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.LIGHT_NYNORSK;
            case MINIMAL_NYNORSK:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.MINIMAL_NYNORSK;
            case PORTUGUESE:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.PORTUGUESE;
            case LIGHT_PORTUGUESE:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.LIGHT_PORTUGUESE;
            case MINIMAL_PORTUGUESE:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.MINIMAL_PORTUGUESE;
            case PORTUGUESE_RSLP:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.PORTUGUESE_RSLP;
            case ROMANIAN:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.ROMANIAN;
            case RUSSIAN:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.RUSSIAN;
            case LIGHT_RUSSIAN:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.LIGHT_RUSSIAN;
            case SPANISH:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.SPANISH;
            case LIGHT_SPANISH:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.LIGHT_SPANISH;
            case SWEDISH:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.SWEDISH;
            case LIGHT_SWEDISH:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.LIGHT_SWEDISH;
            case TURKISH:
                return com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage.TURKISH;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_INTERNAL_ERROR_MSG, obj)));
        }
    }

    private StemmerTokenFilterLanguageConverter() {
    }
}
