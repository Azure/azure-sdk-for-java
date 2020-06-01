// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.KeywordTokenizer;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.KeywordTokenizerV2} and
 * {@link KeywordTokenizer}.
 */
public final class KeywordTokenizerConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.KeywordTokenizerV2} to
     * {@link KeywordTokenizer}.
     */
    public static KeywordTokenizer map(com.azure.search.documents.indexes.implementation.models.KeywordTokenizerV2 obj) {
        if (obj == null) {
            return null;
        }
        KeywordTokenizer keywordTokenizer = new KeywordTokenizer();

        String name = obj.getName();
        keywordTokenizer.setName(name);

        Integer maxTokenLength = obj.getMaxTokenLength();
        keywordTokenizer.setMaxTokenLength(maxTokenLength);
        return keywordTokenizer;
    }

    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.KeywordTokenizer} to
     * {@link KeywordTokenizer}.
     */
    public static KeywordTokenizer map(com.azure.search.documents.indexes.implementation.models.KeywordTokenizer obj) {
        if (obj == null) {
            return null;
        }
        KeywordTokenizer keywordTokenizer = new KeywordTokenizer();

        String name = obj.getName();
        keywordTokenizer.setName(name);

        Integer bufferSize = obj.getBufferSize();
        keywordTokenizer.setMaxTokenLength(bufferSize);
        return keywordTokenizer;
    }

    /**
     * Maps from {@link KeywordTokenizer} to
     * {@link com.azure.search.documents.indexes.implementation.models.KeywordTokenizerV2}.
     */
    public static com.azure.search.documents.indexes.implementation.models.KeywordTokenizerV2 map(KeywordTokenizer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.KeywordTokenizerV2 keywordTokenizerV2 =
            new com.azure.search.documents.indexes.implementation.models.KeywordTokenizerV2();

        String name = obj.getName();
        keywordTokenizerV2.setName(name);

        Integer maxTokenLength = obj.getMaxTokenLength();
        keywordTokenizerV2.setMaxTokenLength(maxTokenLength);
        return keywordTokenizerV2;
    }

    private KeywordTokenizerConverter() {
    }
}
