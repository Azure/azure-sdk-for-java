// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.SearchSuggester;

import java.util.ArrayList;
import java.util.List;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.Suggester} and {@link SearchSuggester}.
 */
public final class SuggesterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.Suggester} to {@link SearchSuggester}.
     */
    public static SearchSuggester map(com.azure.search.documents.indexes.implementation.models.Suggester obj) {
        if (obj == null) {
            return null;
        }
        SearchSuggester searchSuggester = new SearchSuggester();

        if (obj.getSourceFields() != null) {
            List<String> sourceFields = new ArrayList<>(obj.getSourceFields());
            searchSuggester.setSourceFields(sourceFields);
        }

        String name = obj.getName();
        searchSuggester.setName(name);

        return searchSuggester;
    }

    /**
     * Maps from {@link SearchSuggester} to {@link com.azure.search.documents.indexes.implementation.models.Suggester}.
     */
    public static com.azure.search.documents.indexes.implementation.models.Suggester map(SearchSuggester obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.Suggester suggester =
            new com.azure.search.documents.indexes.implementation.models.Suggester();

        if (obj.getSourceFields() != null) {
            List<String> sourceFields = new ArrayList<>(obj.getSourceFields());
            suggester.setSourceFields(sourceFields);
        }

        String name = obj.getName();
        suggester.setName(name);

        return suggester;
    }

    private SuggesterConverter() {
    }
}
