// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.LuceneStandardTokenizer;

/**
 * The helper class to set the non-public properties of an {@link LuceneStandardTokenizer} instance.
 */
public final class LuceneStandardTokenizerHelper {
    private static LuceneStandardTokenizerAccessor accessor;

    private LuceneStandardTokenizerHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link LuceneStandardTokenizer} instance.
     */
    public interface LuceneStandardTokenizerAccessor {
        void setODataType(LuceneStandardTokenizer standardTokenizer, String odataType);
        String getODataType(LuceneStandardTokenizer standardTokenizer);
    }

    /**
     * The method called from {@link LuceneStandardTokenizer} to set it's accessor.
     *
     * @param tokenFilterAccessor The accessor.
     */
    public static void setAccessor(final LuceneStandardTokenizerAccessor tokenFilterAccessor) {
        accessor = tokenFilterAccessor;
    }

    static void setODataType(LuceneStandardTokenizer standardTokenizer, String odataType) {
        accessor.setODataType(standardTokenizer, odataType);
    }

    static String getODataType(LuceneStandardTokenizer standardTokenizer) {
        return accessor.getODataType(standardTokenizer);
    }
}
