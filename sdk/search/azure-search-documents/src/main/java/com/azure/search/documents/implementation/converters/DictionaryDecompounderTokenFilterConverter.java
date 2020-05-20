// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.DictionaryDecompounderTokenFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.DictionaryDecompounderTokenFilter} and
 * {@link DictionaryDecompounderTokenFilter}.
 */
public final class DictionaryDecompounderTokenFilterConverter {
    private static final ClientLogger LOGGER = new ClientLogger(DictionaryDecompounderTokenFilterConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.DictionaryDecompounderTokenFilter} to
     * {@link DictionaryDecompounderTokenFilter}.
     */
    public static DictionaryDecompounderTokenFilter map(com.azure.search.documents.implementation.models.DictionaryDecompounderTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        DictionaryDecompounderTokenFilter dictionaryDecompounderTokenFilter = new DictionaryDecompounderTokenFilter();

        String _name = obj.getName();
        dictionaryDecompounderTokenFilter.setName(_name);

        Integer _minSubwordSize = obj.getMinSubwordSize();
        dictionaryDecompounderTokenFilter.setMinSubwordSize(_minSubwordSize);

        Boolean _onlyLongestMatch = obj.isOnlyLongestMatch();
        dictionaryDecompounderTokenFilter.setOnlyLongestMatch(_onlyLongestMatch);

        Integer _maxSubwordSize = obj.getMaxSubwordSize();
        dictionaryDecompounderTokenFilter.setMaxSubwordSize(_maxSubwordSize);

        if (obj.getWordList() != null) {
            List<String> _wordList = new ArrayList<>(obj.getWordList());
            dictionaryDecompounderTokenFilter.setWordList(_wordList);
        }

        Integer _minWordSize = obj.getMinWordSize();
        dictionaryDecompounderTokenFilter.setMinWordSize(_minWordSize);
        return dictionaryDecompounderTokenFilter;
    }

    /**
     * Maps from {@link DictionaryDecompounderTokenFilter} to
     * {@link com.azure.search.documents.implementation.models.DictionaryDecompounderTokenFilter}.
     */
    public static com.azure.search.documents.implementation.models.DictionaryDecompounderTokenFilter map(DictionaryDecompounderTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.DictionaryDecompounderTokenFilter dictionaryDecompounderTokenFilter = new com.azure.search.documents.implementation.models.DictionaryDecompounderTokenFilter();

        String _name = obj.getName();
        dictionaryDecompounderTokenFilter.setName(_name);

        Integer _minSubwordSize = obj.getMinSubwordSize();
        dictionaryDecompounderTokenFilter.setMinSubwordSize(_minSubwordSize);

        Boolean _onlyLongestMatch = obj.isOnlyLongestMatch();
        dictionaryDecompounderTokenFilter.setOnlyLongestMatch(_onlyLongestMatch);

        Integer _maxSubwordSize = obj.getMaxSubwordSize();
        dictionaryDecompounderTokenFilter.setMaxSubwordSize(_maxSubwordSize);

        if (obj.getWordList() != null) {
            List<String> _wordList = new ArrayList<>(obj.getWordList());
            dictionaryDecompounderTokenFilter.setWordList(_wordList);
        }

        Integer _minWordSize = obj.getMinWordSize();
        dictionaryDecompounderTokenFilter.setMinWordSize(_minWordSize);
        return dictionaryDecompounderTokenFilter;
    }
}
