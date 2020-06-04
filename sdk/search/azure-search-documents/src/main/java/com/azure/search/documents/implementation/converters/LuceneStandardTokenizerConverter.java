// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.indexes.models.LuceneStandardTokenizer;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.LuceneStandardTokenizer} and
 * {@link LuceneStandardTokenizer}.
 */
public final class LuceneStandardTokenizerConverter {
    private static final String V1_ODATA_TYPE = "#Microsoft.Azure.Search.LuceneStandardTokenizer";
    private static final String V2_ODATA_TYPE = "#Microsoft.Azure.Search.LuceneStandardTokenizerV2";
    private static final String ODATA_FIELD_NAME = "odataType";

    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.LuceneStandardTokenizer} to
     * {@link LuceneStandardTokenizer}.
     */
    public static LuceneStandardTokenizer map(com.azure.search.documents.indexes.implementation.models.LuceneStandardTokenizer obj) {
        if (obj == null) {
            return null;
        }
        LuceneStandardTokenizer luceneStandardTokenizer = new LuceneStandardTokenizer();
        PrivateFieldAccessHelper.set(luceneStandardTokenizer, ODATA_FIELD_NAME, V1_ODATA_TYPE);

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
        PrivateFieldAccessHelper.set(luceneStandardTokenizer, ODATA_FIELD_NAME, V2_ODATA_TYPE);

        String name = obj.getName();
        luceneStandardTokenizer.setName(name);

        Integer maxTokenLength = obj.getMaxTokenLength();
        luceneStandardTokenizer.setMaxTokenLength(maxTokenLength);
        return luceneStandardTokenizer;
    }

    /**
     * Maps from {@link LuceneStandardTokenizer} to
     * {@link com.azure.search.documents.indexes.implementation.models.LuceneStandardTokenizerV2} or
     * {@link com.azure.search.documents.indexes.implementation.models.LuceneStandardTokenizerV2} depends on @odata.type
     */
    public static com.azure.search.documents.indexes.implementation.models.LexicalTokenizer map(LuceneStandardTokenizer obj) {
        if (obj == null) {
            return null;
        }
        String identifier = PrivateFieldAccessHelper.get(obj, ODATA_FIELD_NAME, String.class);
        if (V1_ODATA_TYPE.equals(identifier)) {
            return new com.azure.search.documents.indexes.implementation.models.LuceneStandardTokenizer()
                .setMaxTokenLength(obj.getMaxTokenLength())
                .setName(obj.getName());
        } else {
            return new com.azure.search.documents.indexes.implementation.models.LuceneStandardTokenizerV2()
                .setMaxTokenLength(obj.getMaxTokenLength())
                .setName(obj.getName());
        }
    }

    private LuceneStandardTokenizerConverter() {
    }
}
