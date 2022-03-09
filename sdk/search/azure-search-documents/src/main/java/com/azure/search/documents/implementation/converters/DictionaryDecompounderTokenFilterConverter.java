// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.DictionaryDecompounderTokenFilter;

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

        return new DictionaryDecompounderTokenFilter(obj.getName(), obj.getWordList())
            .setMaxSubwordSize(obj.getMaxSubwordSize())
            .setMinSubwordSize(obj.getMinSubwordSize())
            .setMinWordSize(obj.getMinWordSize())
            .setOnlyLongestMatched(obj.isOnlyLongestMatch());
    }

    /**
     * Maps from {@link DictionaryDecompounderTokenFilter} to
     * {@link com.azure.search.documents.indexes.implementation.models.DictionaryDecompounderTokenFilter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.DictionaryDecompounderTokenFilter map(DictionaryDecompounderTokenFilter obj) {
        if (obj == null) {
            return null;
        }

        return new com.azure.search.documents.indexes.implementation.models.DictionaryDecompounderTokenFilter(
            obj.getName(), obj.getWordList())
            .setMaxSubwordSize(obj.getMaxSubwordSize())
            .setMinSubwordSize(obj.getMinSubwordSize())
            .setMinWordSize(obj.getMinWordSize())
            .setOnlyLongestMatch(obj.isOnlyLongestMatched());
    }

    private DictionaryDecompounderTokenFilterConverter() {
    }
}
