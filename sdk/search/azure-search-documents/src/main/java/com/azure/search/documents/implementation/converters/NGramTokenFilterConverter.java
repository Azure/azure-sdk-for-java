// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.NGramTokenFilter;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.NGramTokenFilter} and
 * {@link NGramTokenFilter}.
 */
public final class NGramTokenFilterConverter {
    private static final String V1_ODATA_TYPE = "#Microsoft.Azure.Search.NGramTokenFilter";
    private static final String V2_ODATA_TYPE = "#Microsoft.Azure.Search.NGramTokenFilterV2";
    private static final String ODATA_FIELD_NAME = "odataType";

    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.NGramTokenFilter} to {@link NGramTokenFilter}.
     */
    public static NGramTokenFilter map(com.azure.search.documents.indexes.implementation.models.NGramTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        NGramTokenFilter nGramTokenFilter = new NGramTokenFilter(obj.getName());
        NGramTokenFilterHelper.setODataType(nGramTokenFilter, V1_ODATA_TYPE);

        Integer maxGram = obj.getMaxGram();
        nGramTokenFilter.setMaxGram(maxGram);

        Integer minGram = obj.getMinGram();
        nGramTokenFilter.setMinGram(minGram);
        return nGramTokenFilter;
    }

    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.NGramTokenFilterV2} to {@link NGramTokenFilter}.
     */
    public static NGramTokenFilter map(com.azure.search.documents.indexes.implementation.models.NGramTokenFilterV2 obj) {
        if (obj == null) {
            return null;
        }
        NGramTokenFilter nGramTokenFilter = new NGramTokenFilter(obj.getName());
        NGramTokenFilterHelper.setODataType(nGramTokenFilter, V2_ODATA_TYPE);

        Integer maxGram = obj.getMaxGram();
        nGramTokenFilter.setMaxGram(maxGram);

        Integer minGram = obj.getMinGram();
        nGramTokenFilter.setMinGram(minGram);
        return nGramTokenFilter;
    }

    /**
     * Maps from {@link NGramTokenFilter} to
     * {@link com.azure.search.documents.indexes.implementation.models.NGramTokenFilterV2} or
     * {@link com.azure.search.documents.indexes.implementation.models.NGramTokenFilterV2} depends on @odata.type.
     */
    public static com.azure.search.documents.indexes.implementation.models.TokenFilter map(NGramTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        String identifier = NGramTokenFilterHelper.getODataType(obj);
        if (V1_ODATA_TYPE.equals(identifier)) {
            return new com.azure.search.documents.indexes.implementation.models.NGramTokenFilter(obj.getName())
                .setMaxGram(obj.getMaxGram())
                .setMinGram(obj.getMinGram());
        } else {
            return new com.azure.search.documents.indexes.implementation.models.NGramTokenFilterV2(obj.getName())
                .setMaxGram(obj.getMaxGram())
                .setMinGram(obj.getMinGram());
        }
    }

    private NGramTokenFilterConverter() {
    }
}
