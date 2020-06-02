// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.SynonymTokenFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.SynonymTokenFilter} and
 * {@link SynonymTokenFilter}.
 */
public final class SynonymTokenFilterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.SynonymTokenFilter} to
     * {@link SynonymTokenFilter}.
     */
    public static SynonymTokenFilter map(com.azure.search.documents.indexes.implementation.models.SynonymTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        SynonymTokenFilter synonymTokenFilter = new SynonymTokenFilter();

        String name = obj.getName();
        synonymTokenFilter.setName(name);

        Boolean expand = obj.isExpand();
        synonymTokenFilter.setExpand(expand);

        if (obj.getSynonyms() != null) {
            List<String> synonyms = new ArrayList<>(obj.getSynonyms());
            synonymTokenFilter.setSynonyms(synonyms);
        }

        Boolean ignoreCase = obj.isIgnoreCase();
        synonymTokenFilter.setIgnoreCase(ignoreCase);
        return synonymTokenFilter;
    }

    /**
     * Maps from {@link SynonymTokenFilter} to
     * {@link com.azure.search.documents.indexes.implementation.models.SynonymTokenFilter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.SynonymTokenFilter map(SynonymTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.SynonymTokenFilter synonymTokenFilter =
            new com.azure.search.documents.indexes.implementation.models.SynonymTokenFilter();

        String name = obj.getName();
        synonymTokenFilter.setName(name);

        Boolean expand = obj.isExpand();
        synonymTokenFilter.setExpand(expand);

        if (obj.getSynonyms() != null) {
            List<String> synonyms = new ArrayList<>(obj.getSynonyms());
            synonymTokenFilter.setSynonyms(synonyms);
        }

        Boolean ignoreCase = obj.isIgnoreCase();
        synonymTokenFilter.setIgnoreCase(ignoreCase);
        return synonymTokenFilter;
    }

    private SynonymTokenFilterConverter() {
    }
}
