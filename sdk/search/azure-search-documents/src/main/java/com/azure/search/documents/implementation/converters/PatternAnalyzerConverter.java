// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.PatternAnalyzer;
import com.azure.search.documents.indexes.models.RegexFlags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.PatternAnalyzer} and
 * {@link PatternAnalyzer}.
 */
public final class PatternAnalyzerConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.PatternAnalyzer} to {@link PatternAnalyzer}.
     */
    public static PatternAnalyzer map(com.azure.search.documents.indexes.implementation.models.PatternAnalyzer obj) {
        if (obj == null) {
            return null;
        }
        PatternAnalyzer patternAnalyzer = new PatternAnalyzer(obj.getName());

        Boolean lowerCaseTerms = obj.isLowerCaseTerms();
        patternAnalyzer.setLowerCaseTerms(lowerCaseTerms);

        String pattern = obj.getPattern();
        patternAnalyzer.setPattern(pattern);

        if (obj.getFlags() != null) {
            patternAnalyzer.setFlags(Arrays.stream(obj.getFlags().toString().split("\\|"))
                .map(RegexFlags::fromString)
                .collect(Collectors.toList()));
        }

        if (obj.getStopwords() != null) {
            patternAnalyzer.setStopwords(obj.getStopwords());
        }
        return patternAnalyzer;
    }

    /**
     * Maps from {@link PatternAnalyzer} to {@link com.azure.search.documents.indexes.implementation.models.PatternAnalyzer}.
     */
    public static com.azure.search.documents.indexes.implementation.models.PatternAnalyzer map(PatternAnalyzer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.PatternAnalyzer patternAnalyzer =
            new com.azure.search.documents.indexes.implementation.models.PatternAnalyzer(obj.getName());

        Boolean lowerCaseTerms = obj.areLowerCaseTerms();
        patternAnalyzer.setLowerCaseTerms(lowerCaseTerms);

        String pattern = obj.getPattern();
        patternAnalyzer.setPattern(pattern);

        if (obj.getFlags() != null) {
            String flattenFlags = obj.getFlags().stream().map(RegexFlags::toString).collect(Collectors.joining("|"));
            patternAnalyzer.setFlags(RegexFlags.fromString(flattenFlags));
        }

        if (obj.getStopwords() != null) {
            List<String> stopwords = new ArrayList<>(obj.getStopwords());
            patternAnalyzer.setStopwords(stopwords);
        }

        return patternAnalyzer;
    }

    private PatternAnalyzerConverter() {
    }
}
