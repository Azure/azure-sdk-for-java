// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.PatternTokenizer;
import com.azure.search.documents.indexes.models.RegexFlags;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.PatternTokenizer} and
 * {@link PatternTokenizer}.
 */
public final class PatternTokenizerConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.PatternTokenizer} to {@link PatternTokenizer}.
     */
    public static PatternTokenizer map(com.azure.search.documents.indexes.implementation.models.PatternTokenizer obj) {
        if (obj == null) {
            return null;
        }
        PatternTokenizer patternTokenizer = new PatternTokenizer(obj.getName());

        String pattern = obj.getPattern();
        patternTokenizer.setPattern(pattern);

        if (obj.getFlags() != null) {
            patternTokenizer.setFlags(Arrays.stream(obj.getFlags().toString().split("\\|"))
                .map(RegexFlags::fromString)
                .collect(Collectors.toList()));
        }

        Integer group = obj.getGroup();
        patternTokenizer.setGroup(group);
        return patternTokenizer;
    }

    /**
     * Maps from {@link PatternTokenizer} to {@link com.azure.search.documents.indexes.implementation.models.PatternTokenizer}.
     */
    public static com.azure.search.documents.indexes.implementation.models.PatternTokenizer map(PatternTokenizer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.PatternTokenizer patternTokenizer =
            new com.azure.search.documents.indexes.implementation.models.PatternTokenizer(obj.getName());

        String pattern = obj.getPattern();
        patternTokenizer.setPattern(pattern);

        if (obj.getFlags() != null) {
            String flattenFlags = obj.getFlags().stream().map(RegexFlags::toString).collect(Collectors.joining("|"));
            patternTokenizer.setFlags(RegexFlags.fromString(flattenFlags));
        }

        Integer group = obj.getGroup();
        patternTokenizer.setGroup(group);

        return patternTokenizer;
    }

    private PatternTokenizerConverter() {
    }
}
