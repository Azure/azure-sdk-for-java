// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.PatternReplaceCharFilter;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.PatternReplaceCharFilter} and
 * {@link PatternReplaceCharFilter}.
 */
public final class PatternReplaceCharFilterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.PatternReplaceCharFilter} to
     * {@link PatternReplaceCharFilter}.
     */
    public static PatternReplaceCharFilter map(com.azure.search.documents.indexes.implementation.models.PatternReplaceCharFilter obj) {
        if (obj == null) {
            return null;
        }
        PatternReplaceCharFilter patternReplaceCharFilter = new PatternReplaceCharFilter();

        String name = obj.getName();
        patternReplaceCharFilter.setName(name);

        String pattern = obj.getPattern();
        patternReplaceCharFilter.setPattern(pattern);

        String replacement = obj.getReplacement();
        patternReplaceCharFilter.setReplacement(replacement);
        return patternReplaceCharFilter;
    }

    /**
     * Maps from {@link PatternReplaceCharFilter} to
     * {@link com.azure.search.documents.indexes.implementation.models.PatternReplaceCharFilter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.PatternReplaceCharFilter map(PatternReplaceCharFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.PatternReplaceCharFilter patternReplaceCharFilter =
            new com.azure.search.documents.indexes.implementation.models.PatternReplaceCharFilter();

        String name = obj.getName();
        patternReplaceCharFilter.setName(name);

        String pattern = obj.getPattern();
        patternReplaceCharFilter.setPattern(pattern);

        String replacement = obj.getReplacement();
        patternReplaceCharFilter.setReplacement(replacement);
        return patternReplaceCharFilter;
    }

    private PatternReplaceCharFilterConverter() {
    }
}
