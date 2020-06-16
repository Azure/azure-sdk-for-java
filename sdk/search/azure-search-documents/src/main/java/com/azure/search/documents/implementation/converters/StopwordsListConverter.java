package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.indexes.models.StopwordsList;

import static com.azure.search.documents.implementation.util.Constants.ENUM_EXTERNAL_ERROR_MSG;
import static com.azure.search.documents.implementation.util.Constants.ENUM_INTERNAL_ERROR_MSG;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.StopwordsList} and {@link StopwordsList}.
 */
public final class StopwordsListConverter {
    private static final ClientLogger LOGGER = new ClientLogger(StopwordsListConverter.class);

    /**
     * Maps from enum {@link com.azure.search.documents.indexes.implementation.models.StopwordsList} to enum {@link StopwordsList}.
     */
    public static StopwordsList map(
        com.azure.search.documents.indexes.implementation.models.StopwordsList obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case ARABIC:
                return StopwordsList.ARABIC;
            case ARMENIAN:
                return StopwordsList.ARMENIAN;
            case BASQUE:
                return StopwordsList.BASQUE;
            case BRAZILIAN:
                return StopwordsList.BRAZILIAN;
            case BULGARIAN:
                return StopwordsList.BULGARIAN;
            case CATALAN:
                return StopwordsList.CATALAN;
            case CZECH:
                return StopwordsList.CZECH;
            case DANISH:
                return StopwordsList.DANISH;
            case DUTCH:
                return StopwordsList.DUTCH;
            case ENGLISH:
                return StopwordsList.ENGLISH;
            case FINNISH:
                return StopwordsList.FINNISH;
            case FRENCH:
                return StopwordsList.FRENCH;
            case GALICIAN:
                return StopwordsList.GALICIAN;
            case GERMAN:
                return StopwordsList.GERMAN;
            case GREEK:
                return StopwordsList.GREEK;
            case HINDI:
                return StopwordsList.HINDI;
            case HUNGARIAN:
                return StopwordsList.HUNGARIAN;
            case INDONESIAN:
                return StopwordsList.INDONESIAN;
            case IRISH:
                return StopwordsList.IRISH;
            case ITALIAN:
                return StopwordsList.ITALIAN;
            case LATVIAN:
                return StopwordsList.LATVIAN;
            case NORWEGIAN:
                return StopwordsList.NORWEGIAN;
            case PERSIAN:
                return StopwordsList.PERSIAN;
            case PORTUGUESE:
                return StopwordsList.PORTUGUESE;
            case ROMANIAN:
                return StopwordsList.ROMANIAN;
            case RUSSIAN:
                return StopwordsList.RUSSIAN;
            case SORANI:
                return StopwordsList.SORANI;
            case SPANISH:
                return StopwordsList.SPANISH;
            case SWEDISH:
                return StopwordsList.SWEDISH;
            case THAI:
                return StopwordsList.THAI;
            case TURKISH:
                return StopwordsList.TURKISH;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_EXTERNAL_ERROR_MSG, obj)));
        }
    }

    /**
     * Maps from enum {@link StopwordsList} to enum {@link com.azure.search.documents.indexes.implementation.models.StopwordsList}.
     */
    public static com.azure.search.documents.indexes.implementation.models.StopwordsList map(
        StopwordsList obj) {
        if (obj == null) {
            return null;
        }
        switch (obj) {
            case ARABIC:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.ARABIC;
            case ARMENIAN:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.ARMENIAN;
            case BASQUE:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.BASQUE;
            case BRAZILIAN:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.BRAZILIAN;
            case BULGARIAN:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.BULGARIAN;
            case CATALAN:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.CATALAN;
            case CZECH:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.CZECH;
            case DANISH:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.DANISH;
            case DUTCH:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.DUTCH;
            case ENGLISH:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.ENGLISH;
            case FINNISH:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.FINNISH;
            case FRENCH:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.FRENCH;
            case GALICIAN:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.GALICIAN;
            case GERMAN:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.GERMAN;
            case GREEK:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.GREEK;
            case HINDI:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.HINDI;
            case HUNGARIAN:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.HUNGARIAN;
            case INDONESIAN:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.INDONESIAN;
            case IRISH:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.IRISH;
            case ITALIAN:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.ITALIAN;
            case LATVIAN:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.LATVIAN;
            case NORWEGIAN:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.NORWEGIAN;
            case PERSIAN:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.PERSIAN;
            case PORTUGUESE:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.PORTUGUESE;
            case ROMANIAN:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.ROMANIAN;
            case RUSSIAN:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.RUSSIAN;
            case SORANI:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.SORANI;
            case SPANISH:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.SPANISH;
            case SWEDISH:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.SWEDISH;
            case THAI:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.THAI;
            case TURKISH:
                return com.azure.search.documents.indexes.implementation.models.StopwordsList.TURKISH;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format(ENUM_INTERNAL_ERROR_MSG, obj)));
        }
    }
}
