// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.NGramTokenFilterV2;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.NGramTokenFilterV2} and
 * {@link NGramTokenFilterV2}.
 */
public final class NGramTokenFilterV2Converter {
    /**
     * Maps from {@link com.azure.search.documents.implementation.models.NGramTokenFilterV2} to
     * {@link NGramTokenFilterV2}.
     */
    public static NGramTokenFilterV2 map(com.azure.search.documents.implementation.models.NGramTokenFilterV2 obj) {
        if (obj == null) {
            return null;
        }
        NGramTokenFilterV2 nGramTokenFilterV2 = new NGramTokenFilterV2();

        String name = obj.getName();
        nGramTokenFilterV2.setName(name);

        Integer maxGram = obj.getMaxGram();
        nGramTokenFilterV2.setMaxGram(maxGram);

        Integer minGram = obj.getMinGram();
        nGramTokenFilterV2.setMinGram(minGram);
        return nGramTokenFilterV2;
    }

    /**
     * Maps from {@link NGramTokenFilterV2} to
     * {@link com.azure.search.documents.implementation.models.NGramTokenFilterV2}.
     */
    public static com.azure.search.documents.implementation.models.NGramTokenFilterV2 map(NGramTokenFilterV2 obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.NGramTokenFilterV2 nGramTokenFilterV2 =
            new com.azure.search.documents.implementation.models.NGramTokenFilterV2();

        String name = obj.getName();
        nGramTokenFilterV2.setName(name);

        Integer maxGram = obj.getMaxGram();
        nGramTokenFilterV2.setMaxGram(maxGram);

        Integer minGram = obj.getMinGram();
        nGramTokenFilterV2.setMinGram(minGram);
        return nGramTokenFilterV2;
    }

    private NGramTokenFilterV2Converter() {
    }
}
