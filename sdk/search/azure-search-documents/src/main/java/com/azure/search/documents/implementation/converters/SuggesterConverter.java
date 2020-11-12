// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.SearchSuggester;

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
        return new SearchSuggester(obj.getName(), obj.getSourceFields());
    }

    /**
     * Maps from {@link SearchSuggester} to {@link com.azure.search.documents.indexes.implementation.models.Suggester}.
     */
    public static com.azure.search.documents.indexes.implementation.models.Suggester map(SearchSuggester obj) {
        if (obj == null) {
            return null;
        }

        return new com.azure.search.documents.indexes.implementation.models.Suggester(obj.getName(),
            "analyzingInfixMatching", obj.getSourceFields());
    }

    private SuggesterConverter() {
    }
}
