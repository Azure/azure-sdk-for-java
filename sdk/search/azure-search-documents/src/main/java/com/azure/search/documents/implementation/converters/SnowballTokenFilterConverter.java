// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.SnowballTokenFilter;
import com.azure.search.documents.indexes.models.SnowballTokenFilterLanguage;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.SnowballTokenFilter} and
 * {@link SnowballTokenFilter}.
 */
public final class SnowballTokenFilterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.SnowballTokenFilter} to
     * {@link SnowballTokenFilter}.
     */
    public static SnowballTokenFilter map(com.azure.search.documents.indexes.implementation.models.SnowballTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        SnowballTokenFilter snowballTokenFilter = new SnowballTokenFilter();

        String name = obj.getName();
        snowballTokenFilter.setName(name);

        if (obj.getLanguage() != null) {
            SnowballTokenFilterLanguage language = SnowballTokenFilterLanguageConverter.map(obj.getLanguage());
            snowballTokenFilter.setLanguage(language);
        }
        return snowballTokenFilter;
    }

    /**
     * Maps from {@link SnowballTokenFilter} to
     * {@link com.azure.search.documents.indexes.implementation.models.SnowballTokenFilter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.SnowballTokenFilter map(SnowballTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.SnowballTokenFilter snowballTokenFilter =
            new com.azure.search.documents.indexes.implementation.models.SnowballTokenFilter();

        String name = obj.getName();
        snowballTokenFilter.setName(name);

        if (obj.getLanguage() != null) {
            com.azure.search.documents.indexes.implementation.models.SnowballTokenFilterLanguage language =
                SnowballTokenFilterLanguageConverter.map(obj.getLanguage());
            snowballTokenFilter.setLanguage(language);
        }
        return snowballTokenFilter;
    }

    private SnowballTokenFilterConverter() {
    }
}
