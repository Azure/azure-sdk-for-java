// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.LuceneStandardTokenizer;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.LuceneStandardTokenizer} and
 * {@link LuceneStandardTokenizer}.
 */
public final class LuceneStandardTokenizerConverter {


    /**
     * Maps from {@link com.azure.search.documents.implementation.models.LuceneStandardTokenizer} to
     * {@link LuceneStandardTokenizer}.
     */
    public static LuceneStandardTokenizer map(com.azure.search.documents.implementation.models.LuceneStandardTokenizer obj) {
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
     * {@link com.azure.search.documents.implementation.models.LuceneStandardTokenizer}.
     */
    public static com.azure.search.documents.implementation.models.LuceneStandardTokenizer map(LuceneStandardTokenizer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.LuceneStandardTokenizer luceneStandardTokenizer =
            new com.azure.search.documents.implementation.models.LuceneStandardTokenizer();

        String name = obj.getName();
        luceneStandardTokenizer.setName(name);

        Integer maxTokenLength = obj.getMaxTokenLength();
        luceneStandardTokenizer.setMaxTokenLength(maxTokenLength);
        return luceneStandardTokenizer;
    }

    private LuceneStandardTokenizerConverter() {
    }
}
