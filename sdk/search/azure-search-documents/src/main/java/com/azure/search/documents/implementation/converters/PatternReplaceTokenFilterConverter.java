// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.PatternReplaceTokenFilter;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.PatternReplaceTokenFilter} and
 * {@link PatternReplaceTokenFilter}.
 */
public final class PatternReplaceTokenFilterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.PatternReplaceTokenFilter} to
     * {@link PatternReplaceTokenFilter}.
     */
    public static PatternReplaceTokenFilter map(com.azure.search.documents.indexes.implementation.models.PatternReplaceTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        PatternReplaceTokenFilter patternReplaceTokenFilter = new PatternReplaceTokenFilter();

        String name = obj.getName();
        patternReplaceTokenFilter.setName(name);

        String pattern = obj.getPattern();
        patternReplaceTokenFilter.setPattern(pattern);

        String replacement = obj.getReplacement();
        patternReplaceTokenFilter.setReplacement(replacement);
        return patternReplaceTokenFilter;
    }

    /**
     * Maps from {@link PatternReplaceTokenFilter} to
     * {@link com.azure.search.documents.indexes.implementation.models.PatternReplaceTokenFilter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.PatternReplaceTokenFilter map(PatternReplaceTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.PatternReplaceTokenFilter patternReplaceTokenFilter =
            new com.azure.search.documents.indexes.implementation.models.PatternReplaceTokenFilter();

        String name = obj.getName();
        patternReplaceTokenFilter.setName(name);

        String pattern = obj.getPattern();
        patternReplaceTokenFilter.setPattern(pattern);

        String replacement = obj.getReplacement();
        patternReplaceTokenFilter.setReplacement(replacement);
        return patternReplaceTokenFilter;
    }

    private PatternReplaceTokenFilterConverter() {
    }
}
