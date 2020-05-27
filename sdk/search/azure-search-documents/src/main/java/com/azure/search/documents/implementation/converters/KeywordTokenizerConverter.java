// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.KeywordTokenizer;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.KeywordTokenizer} and
 * {@link KeywordTokenizer}.
 */
public final class KeywordTokenizerConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.KeywordTokenizer} to {@link KeywordTokenizer}.
     */
    public static KeywordTokenizer map(com.azure.search.documents.indexes.implementation.models.KeywordTokenizer obj) {
        if (obj == null) {
            return null;
        }
        KeywordTokenizer keywordTokenizer = new KeywordTokenizer();

        String name = obj.getName();
        keywordTokenizer.setName(name);

        Integer bufferSize = obj.getBufferSize();
        keywordTokenizer.setBufferSize(bufferSize);
        return keywordTokenizer;
    }

    /**
     * Maps from {@link KeywordTokenizer} to {@link com.azure.search.documents.indexes.implementation.models.KeywordTokenizer}.
     */
    public static com.azure.search.documents.indexes.implementation.models.KeywordTokenizer map(KeywordTokenizer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.KeywordTokenizer keywordTokenizer =
            new com.azure.search.documents.indexes.implementation.models.KeywordTokenizer();

        String name = obj.getName();
        keywordTokenizer.setName(name);

        Integer bufferSize = obj.getBufferSize();
        keywordTokenizer.setBufferSize(bufferSize);
        return keywordTokenizer;
    }

    private KeywordTokenizerConverter() {
    }
}
