// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.KeywordTokenizerV2;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.KeywordTokenizerV2} and
 * {@link KeywordTokenizerV2}.
 */
public final class KeywordTokenizerV2Converter {
    private static final ClientLogger LOGGER = new ClientLogger(KeywordTokenizerV2Converter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.KeywordTokenizerV2} to
     * {@link KeywordTokenizerV2}.
     */
    public static KeywordTokenizerV2 map(com.azure.search.documents.implementation.models.KeywordTokenizerV2 obj) {
        if (obj == null) {
            return null;
        }
        KeywordTokenizerV2 keywordTokenizerV2 = new KeywordTokenizerV2();

        String _name = obj.getName();
        keywordTokenizerV2.setName(_name);

        Integer _maxTokenLength = obj.getMaxTokenLength();
        keywordTokenizerV2.setMaxTokenLength(_maxTokenLength);
        return keywordTokenizerV2;
    }

    /**
     * Maps from {@link KeywordTokenizerV2} to
     * {@link com.azure.search.documents.implementation.models.KeywordTokenizerV2}.
     */
    public static com.azure.search.documents.implementation.models.KeywordTokenizerV2 map(KeywordTokenizerV2 obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.KeywordTokenizerV2 keywordTokenizerV2 =
            new com.azure.search.documents.implementation.models.KeywordTokenizerV2();

        String _name = obj.getName();
        keywordTokenizerV2.setName(_name);

        Integer _maxTokenLength = obj.getMaxTokenLength();
        keywordTokenizerV2.setMaxTokenLength(_maxTokenLength);
        return keywordTokenizerV2;
    }
}
