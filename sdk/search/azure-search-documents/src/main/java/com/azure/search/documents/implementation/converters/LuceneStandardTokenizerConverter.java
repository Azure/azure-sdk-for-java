// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.LuceneStandardTokenizer;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.LuceneStandardTokenizer} and
 * {@link LuceneStandardTokenizer}.
 */
public final class LuceneStandardTokenizerConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.LuceneStandardTokenizer} to
     * {@link LuceneStandardTokenizer}.
     */
    public static LuceneStandardTokenizer map(com.azure.search.documents.indexes.implementation.models.LuceneStandardTokenizer obj) {
        if (obj == null) {
            return null;
        }
        LuceneStandardTokenizer luceneStandardTokenizer = new LuceneStandardTokenizer();

        String name = obj.getName();
        luceneStandardTokenizer.setName(name);

        Integer maxTokenLength = obj.getMaxTokenLength();
        luceneStandardTokenizer.setMaxTokenLength(maxTokenLength);
        return luceneStandardTokenizer;
    }

    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.LuceneStandardTokenizerV2} to
     * {@link LuceneStandardTokenizer}.
     */
    public static LuceneStandardTokenizer map(com.azure.search.documents.indexes.implementation.models.LuceneStandardTokenizerV2 obj) {
        if (obj == null) {
            return null;
        }
        LuceneStandardTokenizer luceneStandardTokenizer = new LuceneStandardTokenizer();

        String name = obj.getName();
        luceneStandardTokenizer.setName(name);

        Integer maxTokenLength = obj.getMaxTokenLength();
        luceneStandardTokenizer.setMaxTokenLength(maxTokenLength);
        return luceneStandardTokenizer;
    }

    /**
     * Maps from {@link LuceneStandardTokenizer} to
     * {@link com.azure.search.documents.indexes.implementation.models.LuceneStandardTokenizerV2}.
     */
    public static com.azure.search.documents.indexes.implementation.models.LuceneStandardTokenizerV2 map(LuceneStandardTokenizer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.LuceneStandardTokenizerV2 luceneStandardTokenizer =
            new com.azure.search.documents.indexes.implementation.models.LuceneStandardTokenizerV2();

        String name = obj.getName();
        luceneStandardTokenizer.setName(name);

        Integer maxTokenLength = obj.getMaxTokenLength();
        luceneStandardTokenizer.setMaxTokenLength(maxTokenLength);
        return luceneStandardTokenizer;
    }

    private LuceneStandardTokenizerConverter() {
    }
}
