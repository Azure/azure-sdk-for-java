// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.StemmerTokenFilter;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.StemmerTokenFilter} and
 * {@link StemmerTokenFilter}.
 */
public final class StemmerTokenFilterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.StemmerTokenFilter} to
     * {@link StemmerTokenFilter}.
     */
    public static StemmerTokenFilter map(com.azure.search.documents.indexes.implementation.models.StemmerTokenFilter obj) {
        if (obj == null) {
            return null;
        }

        return new StemmerTokenFilter(obj.getName(), obj.getLanguage());
    }

    /**
     * Maps from {@link StemmerTokenFilter} to
     * {@link com.azure.search.documents.indexes.implementation.models.StemmerTokenFilter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.StemmerTokenFilter map(StemmerTokenFilter obj) {

        if (obj == null) {
            return null;
        }

        return new com.azure.search.documents.indexes.implementation.models.StemmerTokenFilter(obj.getName(),
            obj.getLanguage());
    }

    private StemmerTokenFilterConverter() {
    }
}
