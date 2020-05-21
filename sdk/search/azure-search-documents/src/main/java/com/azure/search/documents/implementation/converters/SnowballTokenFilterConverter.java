// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.SnowballTokenFilter;
import com.azure.search.documents.models.SnowballTokenFilterLanguage;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.SnowballTokenFilter} and
 * {@link SnowballTokenFilter}.
 */
public final class SnowballTokenFilterConverter {
    private static final ClientLogger LOGGER = new ClientLogger(SnowballTokenFilterConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.SnowballTokenFilter} to
     * {@link SnowballTokenFilter}.
     */
    public static SnowballTokenFilter map(com.azure.search.documents.implementation.models.SnowballTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        SnowballTokenFilter snowballTokenFilter = new SnowballTokenFilter();

        String _name = obj.getName();
        snowballTokenFilter.setName(_name);

        if (obj.getLanguage() != null) {
            SnowballTokenFilterLanguage _language = SnowballTokenFilterLanguageConverter.map(obj.getLanguage());
            snowballTokenFilter.setLanguage(_language);
        }
        return snowballTokenFilter;
    }

    /**
     * Maps from {@link SnowballTokenFilter} to
     * {@link com.azure.search.documents.implementation.models.SnowballTokenFilter}.
     */
    public static com.azure.search.documents.implementation.models.SnowballTokenFilter map(SnowballTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.SnowballTokenFilter snowballTokenFilter =
            new com.azure.search.documents.implementation.models.SnowballTokenFilter();

        String _name = obj.getName();
        snowballTokenFilter.setName(_name);

        if (obj.getLanguage() != null) {
            com.azure.search.documents.implementation.models.SnowballTokenFilterLanguage _language =
                SnowballTokenFilterLanguageConverter.map(obj.getLanguage());
            snowballTokenFilter.setLanguage(_language);
        }
        return snowballTokenFilter;
    }
}
