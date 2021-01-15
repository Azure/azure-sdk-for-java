// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.KeywordTokenizer;

/**
 * The helper class to set the non-public properties of an {@link KeywordTokenizer} instance.
 */
public final class KeywordTokenizerHelper {
    private static KeywordTokenizerAccessor accessor;

    private KeywordTokenizerHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link KeywordTokenizer} instance.
     */
    public interface KeywordTokenizerAccessor {
        void setODataType(KeywordTokenizer keywordTokenizer, String odataType);
        String getODataType(KeywordTokenizer keywordTokenizer);
    }

    /**
     * The method called from {@link KeywordTokenizer} to set it's accessor.
     *
     * @param keywordTokenizerAccessor The accessor.
     */
    public static void setAccessor(final KeywordTokenizerAccessor keywordTokenizerAccessor) {
        accessor = keywordTokenizerAccessor;
    }

    static void setODataType(KeywordTokenizer keywordTokenizer, String odataType) {
        accessor.setODataType(keywordTokenizer, odataType);
    }

    static String getODataType(KeywordTokenizer keywordTokenizer) {
        return accessor.getODataType(keywordTokenizer);
    }
}
