// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.KeywordTokenizer;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.KeywordTokenizerV2} and
 * {@link KeywordTokenizer}.
 */
public final class KeywordTokenizerConverter {
    private static final String V1_ODATA_TYPE = "#Microsoft.Azure.Search.KeywordTokenizer";
    private static final String V2_ODATA_TYPE = "#Microsoft.Azure.Search.KeywordTokenizerV2";
    private static final String ODATA_FIELD_NAME = "odataType";

    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.KeywordTokenizerV2} to
     * {@link KeywordTokenizer}.
     */
    public static KeywordTokenizer map(com.azure.search.documents.indexes.implementation.models.KeywordTokenizerV2 obj) {
        if (obj == null) {
            return null;
        }
        KeywordTokenizer keywordTokenizer = new KeywordTokenizer(obj.getName());
        KeywordTokenizerHelper.setODataType(keywordTokenizer, V2_ODATA_TYPE);

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
        KeywordTokenizer keywordTokenizer = new KeywordTokenizer(obj.getName());
        KeywordTokenizerHelper.setODataType(keywordTokenizer, V1_ODATA_TYPE);

        Integer bufferSize = obj.getBufferSize();
        keywordTokenizer.setMaxTokenLength(bufferSize);
        return keywordTokenizer;
    }

    /**
     * Maps from {@link KeywordTokenizer} to
     * {@link com.azure.search.documents.indexes.implementation.models.KeywordTokenizerV2} or
     * @link com.azure.search.documents.indexes.implementation.models.KeywordTokenizer} depends on @odata.type.
     */
    public static com.azure.search.documents.indexes.implementation.models.LexicalTokenizer map(KeywordTokenizer obj) {
        if (obj == null) {
            return null;
        }

        String identifier = KeywordTokenizerHelper.getODataType(obj);
        if (V1_ODATA_TYPE.equals(identifier)) {
            return new com.azure.search.documents.indexes.implementation.models.KeywordTokenizer(obj.getName())
                .setBufferSize(obj.getMaxTokenLength());
        } else {
            return new com.azure.search.documents.indexes.implementation.models.KeywordTokenizerV2(obj.getName())
                .setMaxTokenLength(obj.getMaxTokenLength());
        }
    }

    private KeywordTokenizerConverter() {
    }
}
