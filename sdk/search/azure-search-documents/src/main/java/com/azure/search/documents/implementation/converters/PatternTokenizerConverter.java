// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.PatternTokenizer;
import com.azure.search.documents.models.RegexFlags;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.PatternTokenizer} and
 * {@link PatternTokenizer}.
 */
public final class PatternTokenizerConverter {
    private static final ClientLogger LOGGER = new ClientLogger(PatternTokenizerConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.PatternTokenizer} to {@link PatternTokenizer}.
     */
    public static PatternTokenizer map(com.azure.search.documents.implementation.models.PatternTokenizer obj) {
        if (obj == null) {
            return null;
        }
        PatternTokenizer patternTokenizer = new PatternTokenizer();

        String _name = obj.getName();
        patternTokenizer.setName(_name);

        String _pattern = obj.getPattern();
        patternTokenizer.setPattern(_pattern);

        if (obj.getFlags() != null) {
            List<RegexFlags> regexFlags =
                Arrays.stream(obj.getFlags().toString().split("\\|")).map(RegexFlags::fromString).collect(Collectors.toList());
            patternTokenizer.setFlags(regexFlags);
        }

        Integer _group = obj.getGroup();
        patternTokenizer.setGroup(_group);
        return patternTokenizer;
    }

    /**
     * Maps from {@link PatternTokenizer} to {@link com.azure.search.documents.implementation.models.PatternTokenizer}.
     */
    public static com.azure.search.documents.implementation.models.PatternTokenizer map(PatternTokenizer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.PatternTokenizer patternTokenizer =
            new com.azure.search.documents.implementation.models.PatternTokenizer();

        String _name = obj.getName();
        patternTokenizer.setName(_name);

        String _pattern = obj.getPattern();
        patternTokenizer.setPattern(_pattern);

        if (obj.getFlags() != null) {
            String flattenFlags =
                obj.getFlags().stream().map(RegexFlags::toString).collect(Collectors.joining("|"));
            patternTokenizer.setFlags(com.azure.search.documents.implementation.models.RegexFlags.fromString(flattenFlags));
        }

        Integer _group = obj.getGroup();
        patternTokenizer.setGroup(_group);
        return patternTokenizer;
    }
}
