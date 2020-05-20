// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.NGramTokenFilter;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.NGramTokenFilter} and
 * {@link NGramTokenFilter}.
 */
public final class NGramTokenFilterConverter {
    private static final ClientLogger LOGGER = new ClientLogger(NGramTokenFilterConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.NGramTokenFilter} to {@link NGramTokenFilter}.
     */
    public static NGramTokenFilter map(com.azure.search.documents.implementation.models.NGramTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        NGramTokenFilter nGramTokenFilter = new NGramTokenFilter();

        String _name = obj.getName();
        nGramTokenFilter.setName(_name);

        Integer _maxGram = obj.getMaxGram();
        nGramTokenFilter.setMaxGram(_maxGram);

        Integer _minGram = obj.getMinGram();
        nGramTokenFilter.setMinGram(_minGram);
        return nGramTokenFilter;
    }

    /**
     * Maps from {@link NGramTokenFilter} to {@link com.azure.search.documents.implementation.models.NGramTokenFilter}.
     */
    public static com.azure.search.documents.implementation.models.NGramTokenFilter map(NGramTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.NGramTokenFilter nGramTokenFilter =
            new com.azure.search.documents.implementation.models.NGramTokenFilter();

        String _name = obj.getName();
        nGramTokenFilter.setName(_name);

        Integer _maxGram = obj.getMaxGram();
        nGramTokenFilter.setMaxGram(_maxGram);

        Integer _minGram = obj.getMinGram();
        nGramTokenFilter.setMinGram(_minGram);
        return nGramTokenFilter;
    }
}
