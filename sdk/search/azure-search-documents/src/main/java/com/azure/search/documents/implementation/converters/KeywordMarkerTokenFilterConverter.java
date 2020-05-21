// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.KeywordMarkerTokenFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.KeywordMarkerTokenFilter} and
 * {@link KeywordMarkerTokenFilter}.
 */
public final class KeywordMarkerTokenFilterConverter {
    private static final ClientLogger LOGGER = new ClientLogger(KeywordMarkerTokenFilterConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.KeywordMarkerTokenFilter} to
     * {@link KeywordMarkerTokenFilter}.
     */
    public static KeywordMarkerTokenFilter map(com.azure.search.documents.implementation.models.KeywordMarkerTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        KeywordMarkerTokenFilter keywordMarkerTokenFilter = new KeywordMarkerTokenFilter();

        String _name = obj.getName();
        keywordMarkerTokenFilter.setName(_name);

        if (obj.getKeywords() != null) {
            List<String> _keywords = new ArrayList<>(obj.getKeywords());
            keywordMarkerTokenFilter.setKeywords(_keywords);
        }

        Boolean _ignoreCase = obj.isIgnoreCase();
        keywordMarkerTokenFilter.setIgnoreCase(_ignoreCase);
        return keywordMarkerTokenFilter;
    }

    /**
     * Maps from {@link KeywordMarkerTokenFilter} to
     * {@link com.azure.search.documents.implementation.models.KeywordMarkerTokenFilter}.
     */
    public static com.azure.search.documents.implementation.models.KeywordMarkerTokenFilter map(KeywordMarkerTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.KeywordMarkerTokenFilter keywordMarkerTokenFilter =
            new com.azure.search.documents.implementation.models.KeywordMarkerTokenFilter();

        String _name = obj.getName();
        keywordMarkerTokenFilter.setName(_name);

        if (obj.getKeywords() != null) {
            List<String> _keywords = new ArrayList<>(obj.getKeywords());
            keywordMarkerTokenFilter.setKeywords(_keywords);
        }

        Boolean _ignoreCase = obj.isIgnoreCase();
        keywordMarkerTokenFilter.setIgnoreCase(_ignoreCase);
        return keywordMarkerTokenFilter;
    }
}
