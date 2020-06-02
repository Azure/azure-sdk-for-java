// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.NGramTokenFilter;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.NGramTokenFilter} and
 * {@link NGramTokenFilter}.
 */
public final class NGramTokenFilterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.NGramTokenFilter} to {@link NGramTokenFilter}.
     */
    public static NGramTokenFilter map(com.azure.search.documents.indexes.implementation.models.NGramTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        NGramTokenFilter nGramTokenFilter = new NGramTokenFilter();

        String name = obj.getName();
        nGramTokenFilter.setName(name);

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
        NGramTokenFilter nGramTokenFilter = new NGramTokenFilter();

        String name = obj.getName();
        nGramTokenFilter.setName(name);

        Integer maxGram = obj.getMaxGram();
        nGramTokenFilter.setMaxGram(maxGram);

        Integer minGram = obj.getMinGram();
        nGramTokenFilter.setMinGram(minGram);
        return nGramTokenFilter;
    }

    /**
     * Maps from {@link NGramTokenFilter} to {@link com.azure.search.documents.indexes.implementation.models.NGramTokenFilterV2}.
     */
    public static com.azure.search.documents.indexes.implementation.models.NGramTokenFilterV2 map(NGramTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.NGramTokenFilterV2 nGramTokenFilter =
            new com.azure.search.documents.indexes.implementation.models.NGramTokenFilterV2();

        String name = obj.getName();
        nGramTokenFilter.setName(name);

        Integer maxGram = obj.getMaxGram();
        nGramTokenFilter.setMaxGram(maxGram);

        Integer minGram = obj.getMinGram();
        nGramTokenFilter.setMinGram(minGram);
        return nGramTokenFilter;
    }

    private NGramTokenFilterConverter() {
    }
}
