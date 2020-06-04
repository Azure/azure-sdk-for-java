// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.models.MicrosoftStemmingTokenizerLanguage;

import static com.azure.search.documents.implementation.util.Constants.ENUM_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ENUM_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage}
 * and {@link MicrosoftStemmingTokenizerLanguage}.
 */
public final class MicrosoftStemmingTokenizerLanguageConverter {
    private static final ClientLogger LOGGER = new ClientLogger(MicrosoftStemmingTokenizerLanguageConverter.class);

    /**
     * Maps from enum {@link com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage} to
     * enum {@link MicrosoftStemmingTokenizerLanguage}.
     */
    public static MicrosoftStemmingTokenizerLanguage map(com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case ARABIC:
                return MicrosoftStemmingTokenizerLanguage.ARABIC;
            case BANGLA:
                return MicrosoftStemmingTokenizerLanguage.BANGLA;
            case BULGARIAN:
                return MicrosoftStemmingTokenizerLanguage.BULGARIAN;
            case CATALAN:
                return MicrosoftStemmingTokenizerLanguage.CATALAN;
            case CROATIAN:
                return MicrosoftStemmingTokenizerLanguage.CROATIAN;
            case CZECH:
                return MicrosoftStemmingTokenizerLanguage.CZECH;
            case DANISH:
                return MicrosoftStemmingTokenizerLanguage.DANISH;
            case DUTCH:
                return MicrosoftStemmingTokenizerLanguage.DUTCH;
            case ENGLISH:
                return MicrosoftStemmingTokenizerLanguage.ENGLISH;
            case ESTONIAN:
                return MicrosoftStemmingTokenizerLanguage.ESTONIAN;
            case FINNISH:
                return MicrosoftStemmingTokenizerLanguage.FINNISH;
            case FRENCH:
                return MicrosoftStemmingTokenizerLanguage.FRENCH;
            case GERMAN:
                return MicrosoftStemmingTokenizerLanguage.GERMAN;
            case GREEK:
                return MicrosoftStemmingTokenizerLanguage.GREEK;
            case GUJARATI:
                return MicrosoftStemmingTokenizerLanguage.GUJARATI;
            case HEBREW:
                return MicrosoftStemmingTokenizerLanguage.HEBREW;
            case HINDI:
                return MicrosoftStemmingTokenizerLanguage.HINDI;
            case HUNGARIAN:
                return MicrosoftStemmingTokenizerLanguage.HUNGARIAN;
            case ICELANDIC:
                return MicrosoftStemmingTokenizerLanguage.ICELANDIC;
            case INDONESIAN:
                return MicrosoftStemmingTokenizerLanguage.INDONESIAN;
            case ITALIAN:
                return MicrosoftStemmingTokenizerLanguage.ITALIAN;
            case KANNADA:
                return MicrosoftStemmingTokenizerLanguage.KANNADA;
            case LATVIAN:
                return MicrosoftStemmingTokenizerLanguage.LATVIAN;
            case LITHUANIAN:
                return MicrosoftStemmingTokenizerLanguage.LITHUANIAN;
            case MALAY:
                return MicrosoftStemmingTokenizerLanguage.MALAY;
            case MALAYALAM:
                return MicrosoftStemmingTokenizerLanguage.MALAYALAM;
            case MARATHI:
                return MicrosoftStemmingTokenizerLanguage.MARATHI;
            case NORWEGIAN_BOKMAAL:
                return MicrosoftStemmingTokenizerLanguage.NORWEGIAN_BOKMAAL;
            case POLISH:
                return MicrosoftStemmingTokenizerLanguage.POLISH;
            case PORTUGUESE:
                return MicrosoftStemmingTokenizerLanguage.PORTUGUESE;
            case PORTUGUESE_BRAZILIAN:
                return MicrosoftStemmingTokenizerLanguage.PORTUGUESE_BRAZILIAN;
            case PUNJABI:
                return MicrosoftStemmingTokenizerLanguage.PUNJABI;
            case ROMANIAN:
                return MicrosoftStemmingTokenizerLanguage.ROMANIAN;
            case RUSSIAN:
                return MicrosoftStemmingTokenizerLanguage.RUSSIAN;
            case SERBIAN_CYRILLIC:
                return MicrosoftStemmingTokenizerLanguage.SERBIAN_CYRILLIC;
            case SERBIAN_LATIN:
                return MicrosoftStemmingTokenizerLanguage.SERBIAN_LATIN;
            case SLOVAK:
                return MicrosoftStemmingTokenizerLanguage.SLOVAK;
            case SLOVENIAN:
                return MicrosoftStemmingTokenizerLanguage.SLOVENIAN;
            case SPANISH:
                return MicrosoftStemmingTokenizerLanguage.SPANISH;
            case SWEDISH:
                return MicrosoftStemmingTokenizerLanguage.SWEDISH;
            case TAMIL:
                return MicrosoftStemmingTokenizerLanguage.TAMIL;
            case TELUGU:
                return MicrosoftStemmingTokenizerLanguage.TELUGU;
            case TURKISH:
                return MicrosoftStemmingTokenizerLanguage.TURKISH;
            case UKRAINIAN:
                return MicrosoftStemmingTokenizerLanguage.UKRAINIAN;
            case URDU:
                return MicrosoftStemmingTokenizerLanguage.URDU;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_EXTERNAL_ERROR_MSG, obj)));
        }
    }

    /**
     * Maps from enum {@link MicrosoftStemmingTokenizerLanguage} to enum
     * {@link com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage}.
     */
    public static com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage map(MicrosoftStemmingTokenizerLanguage obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case ARABIC:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.ARABIC;
            case BANGLA:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.BANGLA;
            case BULGARIAN:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.BULGARIAN;
            case CATALAN:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.CATALAN;
            case CROATIAN:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.CROATIAN;
            case CZECH:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.CZECH;
            case DANISH:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.DANISH;
            case DUTCH:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.DUTCH;
            case ENGLISH:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.ENGLISH;
            case ESTONIAN:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.ESTONIAN;
            case FINNISH:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.FINNISH;
            case FRENCH:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.FRENCH;
            case GERMAN:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.GERMAN;
            case GREEK:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.GREEK;
            case GUJARATI:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.GUJARATI;
            case HEBREW:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.HEBREW;
            case HINDI:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.HINDI;
            case HUNGARIAN:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.HUNGARIAN;
            case ICELANDIC:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.ICELANDIC;
            case INDONESIAN:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.INDONESIAN;
            case ITALIAN:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.ITALIAN;
            case KANNADA:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.KANNADA;
            case LATVIAN:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.LATVIAN;
            case LITHUANIAN:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.LITHUANIAN;
            case MALAY:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.MALAY;
            case MALAYALAM:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.MALAYALAM;
            case MARATHI:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.MARATHI;
            case NORWEGIAN_BOKMAAL:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.NORWEGIAN_BOKMAAL;
            case POLISH:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.POLISH;
            case PORTUGUESE:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.PORTUGUESE;
            case PORTUGUESE_BRAZILIAN:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.PORTUGUESE_BRAZILIAN;
            case PUNJABI:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.PUNJABI;
            case ROMANIAN:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.ROMANIAN;
            case RUSSIAN:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.RUSSIAN;
            case SERBIAN_CYRILLIC:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.SERBIAN_CYRILLIC;
            case SERBIAN_LATIN:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.SERBIAN_LATIN;
            case SLOVAK:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.SLOVAK;
            case SLOVENIAN:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.SLOVENIAN;
            case SPANISH:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.SPANISH;
            case SWEDISH:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.SWEDISH;
            case TAMIL:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.TAMIL;
            case TELUGU:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.TELUGU;
            case TURKISH:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.TURKISH;
            case UKRAINIAN:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.UKRAINIAN;
            case URDU:
                return com.azure.search.documents.indexes.implementation.models.MicrosoftStemmingTokenizerLanguage.URDU;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_INTERNAL_ERROR_MSG, obj)));
        }
    }

    private MicrosoftStemmingTokenizerLanguageConverter() {
    }
}
