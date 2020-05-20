// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.SynonymTokenFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.SynonymTokenFilter} and
 * {@link SynonymTokenFilter}.
 */
public final class SynonymTokenFilterConverter {
    private static final ClientLogger LOGGER = new ClientLogger(SynonymTokenFilterConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.SynonymTokenFilter} to
     * {@link SynonymTokenFilter}.
     */
    public static SynonymTokenFilter map(com.azure.search.documents.implementation.models.SynonymTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        SynonymTokenFilter synonymTokenFilter = new SynonymTokenFilter();

        String _name = obj.getName();
        synonymTokenFilter.setName(_name);

        Boolean _expand = obj.isExpand();
        synonymTokenFilter.setExpand(_expand);

        if (obj.getSynonyms() != null) {
            List<String> _synonyms = new ArrayList<>(obj.getSynonyms());
            synonymTokenFilter.setSynonyms(_synonyms);
        }

        Boolean _ignoreCase = obj.isIgnoreCase();
        synonymTokenFilter.setIgnoreCase(_ignoreCase);
        return synonymTokenFilter;
    }

    /**
     * Maps from {@link SynonymTokenFilter} to
     * {@link com.azure.search.documents.implementation.models.SynonymTokenFilter}.
     */
    public static com.azure.search.documents.implementation.models.SynonymTokenFilter map(SynonymTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.SynonymTokenFilter synonymTokenFilter =
            new com.azure.search.documents.implementation.models.SynonymTokenFilter();

        String _name = obj.getName();
        synonymTokenFilter.setName(_name);

        Boolean _expand = obj.isExpand();
        synonymTokenFilter.setExpand(_expand);

        if (obj.getSynonyms() != null) {
            List<String> _synonyms = new ArrayList<>(obj.getSynonyms());
            synonymTokenFilter.setSynonyms(_synonyms);
        }

        Boolean _ignoreCase = obj.isIgnoreCase();
        synonymTokenFilter.setIgnoreCase(_ignoreCase);
        return synonymTokenFilter;
    }
}
