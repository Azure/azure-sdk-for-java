// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.WordDelimiterTokenFilter;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.WordDelimiterTokenFilter} and
 * {@link WordDelimiterTokenFilter}.
 */
public final class WordDelimiterTokenFilterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.WordDelimiterTokenFilter} to {@link
     * WordDelimiterTokenFilter}.
     */
    public static WordDelimiterTokenFilter map(com.azure.search.documents.indexes.implementation.models.WordDelimiterTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        WordDelimiterTokenFilter wordDelimiterTokenFilter = new WordDelimiterTokenFilter(obj.getName());

        Boolean catenateNumbers = obj.isCatenateNumbers();
        wordDelimiterTokenFilter.setNumbersCatenated(catenateNumbers);

        wordDelimiterTokenFilter.setProtectedWords(obj.getProtectedWords());

        Boolean generateNumberParts = obj.isGenerateNumberParts();
        wordDelimiterTokenFilter.setGenerateNumberParts(generateNumberParts);

        Boolean stemEnglishPossessive = obj.isStemEnglishPossessive();
        wordDelimiterTokenFilter.setStemEnglishPossessive(stemEnglishPossessive);

        Boolean splitOnCaseChange = obj.isSplitOnCaseChange();
        wordDelimiterTokenFilter.setSplitOnCaseChange(splitOnCaseChange);

        Boolean generateWordParts = obj.isGenerateWordParts();
        wordDelimiterTokenFilter.setGenerateWordParts(generateWordParts);

        Boolean splitOnNumerics = obj.isSplitOnNumerics();
        wordDelimiterTokenFilter.setSplitOnNumerics(splitOnNumerics);

        Boolean preserveOriginal = obj.isPreserveOriginal();
        wordDelimiterTokenFilter.setPreserveOriginal(preserveOriginal);

        Boolean catenateAll = obj.isCatenateAll();
        wordDelimiterTokenFilter.setCatenateAll(catenateAll);

        Boolean catenateWords = obj.isCatenateWords();
        wordDelimiterTokenFilter.setWordsCatenated(catenateWords);
        return wordDelimiterTokenFilter;
    }

    /**
     * Maps from {@link WordDelimiterTokenFilter} to {@link com.azure.search.documents.indexes.implementation.models.WordDelimiterTokenFilter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.WordDelimiterTokenFilter map(WordDelimiterTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.WordDelimiterTokenFilter wordDelimiterTokenFilter =
            new com.azure.search.documents.indexes.implementation.models.WordDelimiterTokenFilter(obj.getName());

        Boolean catenateNumbers = obj.areNumbersCatenated();
        wordDelimiterTokenFilter.setCatenateNumbers(catenateNumbers);

        wordDelimiterTokenFilter.setProtectedWords(obj.getProtectedWords());

        Boolean generateNumberParts = obj.generateNumberParts();
        wordDelimiterTokenFilter.setGenerateNumberParts(generateNumberParts);

        Boolean stemEnglishPossessive = obj.isStemEnglishPossessive();
        wordDelimiterTokenFilter.setStemEnglishPossessive(stemEnglishPossessive);

        Boolean splitOnCaseChange = obj.splitOnCaseChange();
        wordDelimiterTokenFilter.setSplitOnCaseChange(splitOnCaseChange);

        Boolean generateWordParts = obj.generateWordParts();
        wordDelimiterTokenFilter.setGenerateWordParts(generateWordParts);

        Boolean splitOnNumerics = obj.splitOnNumerics();
        wordDelimiterTokenFilter.setSplitOnNumerics(splitOnNumerics);

        Boolean preserveOriginal = obj.isPreserveOriginal();
        wordDelimiterTokenFilter.setPreserveOriginal(preserveOriginal);

        Boolean catenateAll = obj.catenateAll();
        wordDelimiterTokenFilter.setCatenateAll(catenateAll);

        Boolean catenateWords = obj.areWordsCatenated();
        wordDelimiterTokenFilter.setCatenateWords(catenateWords);

        return wordDelimiterTokenFilter;
    }

    private WordDelimiterTokenFilterConverter() {
    }
}
