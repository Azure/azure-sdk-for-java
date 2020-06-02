// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.DictionaryDecompounderTokenFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.DictionaryDecompounderTokenFilter} and
 * {@link DictionaryDecompounderTokenFilter}.
 */
public final class DictionaryDecompounderTokenFilterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.DictionaryDecompounderTokenFilter} to
     * {@link DictionaryDecompounderTokenFilter}.
     */
    public static DictionaryDecompounderTokenFilter map(com.azure.search.documents.indexes.implementation.models.DictionaryDecompounderTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        DictionaryDecompounderTokenFilter dictionaryDecompounderTokenFilter = new DictionaryDecompounderTokenFilter();

        String name = obj.getName();
        dictionaryDecompounderTokenFilter.setName(name);

        Integer minSubwordSize = obj.getMinSubwordSize();
        dictionaryDecompounderTokenFilter.setMinSubwordSize(minSubwordSize);

        Boolean onlyLongestMatch = obj.isOnlyLongestMatch();
        dictionaryDecompounderTokenFilter.setOnlyLongestMatch(onlyLongestMatch);

        Integer maxSubwordSize = obj.getMaxSubwordSize();
        dictionaryDecompounderTokenFilter.setMaxSubwordSize(maxSubwordSize);

        if (obj.getWordList() != null) {
            List<String> wordList = new ArrayList<>(obj.getWordList());
            dictionaryDecompounderTokenFilter.setWordList(wordList);
        }

        Integer minWordSize = obj.getMinWordSize();
        dictionaryDecompounderTokenFilter.setMinWordSize(minWordSize);
        return dictionaryDecompounderTokenFilter;
    }

    /**
     * Maps from {@link DictionaryDecompounderTokenFilter} to
     * {@link com.azure.search.documents.indexes.implementation.models.DictionaryDecompounderTokenFilter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.DictionaryDecompounderTokenFilter map(DictionaryDecompounderTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.DictionaryDecompounderTokenFilter dictionaryDecompounderTokenFilter = new com.azure.search.documents.indexes.implementation.models.DictionaryDecompounderTokenFilter();

        String name = obj.getName();
        dictionaryDecompounderTokenFilter.setName(name);

        Integer minSubwordSize = obj.getMinSubwordSize();
        dictionaryDecompounderTokenFilter.setMinSubwordSize(minSubwordSize);

        Boolean onlyLongestMatch = obj.isOnlyLongestMatch();
        dictionaryDecompounderTokenFilter.setOnlyLongestMatch(onlyLongestMatch);

        Integer maxSubwordSize = obj.getMaxSubwordSize();
        dictionaryDecompounderTokenFilter.setMaxSubwordSize(maxSubwordSize);

        if (obj.getWordList() != null) {
            List<String> wordList = new ArrayList<>(obj.getWordList());
            dictionaryDecompounderTokenFilter.setWordList(wordList);
        }

        Integer minWordSize = obj.getMinWordSize();
        dictionaryDecompounderTokenFilter.setMinWordSize(minWordSize);
        return dictionaryDecompounderTokenFilter;
    }

    private DictionaryDecompounderTokenFilterConverter() {
    }
}
