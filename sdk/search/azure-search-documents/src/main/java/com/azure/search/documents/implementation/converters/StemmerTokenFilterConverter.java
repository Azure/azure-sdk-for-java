// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.StemmerTokenFilter;
import com.azure.search.documents.indexes.models.StemmerTokenFilterLanguage;

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
        StemmerTokenFilter stemmerTokenFilter = new StemmerTokenFilter();

        String name = obj.getName();
        stemmerTokenFilter.setName(name);

        if (obj.getLanguage() != null) {
            StemmerTokenFilterLanguage language = StemmerTokenFilterLanguageConverter.map(obj.getLanguage());
            stemmerTokenFilter.setLanguage(language);
        }
        return stemmerTokenFilter;
    }

    /**
     * Maps from {@link StemmerTokenFilter} to
     * {@link com.azure.search.documents.indexes.implementation.models.StemmerTokenFilter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.StemmerTokenFilter map(StemmerTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.StemmerTokenFilter stemmerTokenFilter =
            new com.azure.search.documents.indexes.implementation.models.StemmerTokenFilter();

        String name = obj.getName();
        stemmerTokenFilter.setName(name);

        if (obj.getLanguage() != null) {
            com.azure.search.documents.indexes.implementation.models.StemmerTokenFilterLanguage language =
                StemmerTokenFilterLanguageConverter.map(obj.getLanguage());
            stemmerTokenFilter.setLanguage(language);
        }
        return stemmerTokenFilter;
    }

    private StemmerTokenFilterConverter() {
    }
}
