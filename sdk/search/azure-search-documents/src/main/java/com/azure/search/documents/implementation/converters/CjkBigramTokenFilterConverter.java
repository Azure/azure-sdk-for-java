// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.CjkBigramTokenFilter;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.CjkBigramTokenFilter} and
 * {@link CjkBigramTokenFilter}.
 */
public final class CjkBigramTokenFilterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.CjkBigramTokenFilter} to
     * {@link CjkBigramTokenFilter}.
     */
    public static CjkBigramTokenFilter map(com.azure.search.documents.indexes.implementation.models.CjkBigramTokenFilter obj) {
        if (obj == null) {
            return null;
        }

        return new CjkBigramTokenFilter(obj.getName())
            .setOutputUnigrams(obj.isOutputUnigrams())
            .setIgnoreScripts(obj.getIgnoreScripts());
    }

    /**
     * Maps from {@link CjkBigramTokenFilter} to
     * {@link com.azure.search.documents.indexes.implementation.models.CjkBigramTokenFilter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.CjkBigramTokenFilter map(CjkBigramTokenFilter obj) {
        if (obj == null) {
            return null;
        }

        return new com.azure.search.documents.indexes.implementation.models.CjkBigramTokenFilter(obj.getName())
            .setOutputUnigrams(obj.areOutputUnigrams())
            .setIgnoreScripts(obj.getIgnoreScripts());
    }

    private CjkBigramTokenFilterConverter() {
    }
}
