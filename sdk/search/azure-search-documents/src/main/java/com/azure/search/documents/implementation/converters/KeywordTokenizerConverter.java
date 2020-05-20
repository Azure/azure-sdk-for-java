// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.KeywordTokenizer;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.KeywordTokenizer} and
 * {@link KeywordTokenizer}.
 */
public final class KeywordTokenizerConverter {
    private static final ClientLogger LOGGER = new ClientLogger(KeywordTokenizerConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.KeywordTokenizer} to {@link KeywordTokenizer}.
     */
    public static KeywordTokenizer map(com.azure.search.documents.implementation.models.KeywordTokenizer obj) {
        if (obj == null) {
            return null;
        }
        KeywordTokenizer keywordTokenizer = new KeywordTokenizer();

        String _name = obj.getName();
        keywordTokenizer.setName(_name);

        Integer _bufferSize = obj.getBufferSize();
        keywordTokenizer.setBufferSize(_bufferSize);
        return keywordTokenizer;
    }

    /**
     * Maps from {@link KeywordTokenizer} to {@link com.azure.search.documents.implementation.models.KeywordTokenizer}.
     */
    public static com.azure.search.documents.implementation.models.KeywordTokenizer map(KeywordTokenizer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.KeywordTokenizer keywordTokenizer =
            new com.azure.search.documents.implementation.models.KeywordTokenizer();

        String _name = obj.getName();
        keywordTokenizer.setName(_name);

        Integer _bufferSize = obj.getBufferSize();
        keywordTokenizer.setBufferSize(_bufferSize);
        return keywordTokenizer;
    }
}
