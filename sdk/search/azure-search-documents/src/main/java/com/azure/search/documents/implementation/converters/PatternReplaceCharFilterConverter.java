package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.PatternReplaceCharFilter;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.PatternReplaceCharFilter} and
 * {@link PatternReplaceCharFilter}.
 */
public final class PatternReplaceCharFilterConverter {
    private static final ClientLogger LOGGER = new ClientLogger(PatternReplaceCharFilterConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.PatternReplaceCharFilter} to
     * {@link PatternReplaceCharFilter}.
     */
    public static PatternReplaceCharFilter map(com.azure.search.documents.implementation.models.PatternReplaceCharFilter obj) {
        if (obj == null) {
            return null;
        }
        PatternReplaceCharFilter patternReplaceCharFilter = new PatternReplaceCharFilter();

        String _name = obj.getName();
        patternReplaceCharFilter.setName(_name);

        String _pattern = obj.getPattern();
        patternReplaceCharFilter.setPattern(_pattern);

        String _replacement = obj.getReplacement();
        patternReplaceCharFilter.setReplacement(_replacement);
        return patternReplaceCharFilter;
    }

    /**
     * Maps from {@link PatternReplaceCharFilter} to
     * {@link com.azure.search.documents.implementation.models.PatternReplaceCharFilter}.
     */
    public static com.azure.search.documents.implementation.models.PatternReplaceCharFilter map(PatternReplaceCharFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.PatternReplaceCharFilter patternReplaceCharFilter =
            new com.azure.search.documents.implementation.models.PatternReplaceCharFilter();

        String _name = obj.getName();
        patternReplaceCharFilter.setName(_name);

        String _pattern = obj.getPattern();
        patternReplaceCharFilter.setPattern(_pattern);

        String _replacement = obj.getReplacement();
        patternReplaceCharFilter.setReplacement(_replacement);
        return patternReplaceCharFilter;
    }
}
