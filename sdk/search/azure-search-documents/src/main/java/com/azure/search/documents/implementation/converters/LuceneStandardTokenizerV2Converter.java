// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.LuceneStandardTokenizerV2;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.LuceneStandardTokenizerV2} and
 * {@link LuceneStandardTokenizerV2}.
 */
public final class LuceneStandardTokenizerV2Converter {


    /**
     * Maps from {@link com.azure.search.documents.implementation.models.LuceneStandardTokenizerV2} to
     * {@link LuceneStandardTokenizerV2}.
     */
    public static LuceneStandardTokenizerV2 map(com.azure.search.documents.implementation.models.LuceneStandardTokenizerV2 obj) {
        if (obj == null) {
            return null;
        }
        LuceneStandardTokenizerV2 luceneStandardTokenizerV2 = new LuceneStandardTokenizerV2();

        String name = obj.getName();
        luceneStandardTokenizerV2.setName(name);

        Integer maxTokenLength = obj.getMaxTokenLength();
        luceneStandardTokenizerV2.setMaxTokenLength(maxTokenLength);
        return luceneStandardTokenizerV2;
    }

    /**
     * Maps from {@link LuceneStandardTokenizerV2} to
     * {@link com.azure.search.documents.implementation.models.LuceneStandardTokenizerV2}.
     */
    public static com.azure.search.documents.implementation.models.LuceneStandardTokenizerV2 map(LuceneStandardTokenizerV2 obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.LuceneStandardTokenizerV2 luceneStandardTokenizerV2 =
            new com.azure.search.documents.implementation.models.LuceneStandardTokenizerV2();

        String name = obj.getName();
        luceneStandardTokenizerV2.setName(name);

        Integer maxTokenLength = obj.getMaxTokenLength();
        luceneStandardTokenizerV2.setMaxTokenLength(maxTokenLength);
        return luceneStandardTokenizerV2;
    }

    private LuceneStandardTokenizerV2Converter() {
    }
}
