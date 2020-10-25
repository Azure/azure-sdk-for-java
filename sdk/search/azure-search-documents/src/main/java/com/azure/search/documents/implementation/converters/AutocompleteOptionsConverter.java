// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.models.AutocompleteOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.AutocompleteOptions} and
 * {@link AutocompleteOptions}.
 */
public final class AutocompleteOptionsConverter {
    /**
     * Maps from {@link com.azure.search.documents.implementation.models.AutocompleteOptions} to
     * {@link AutocompleteOptions}.
     */
    public static AutocompleteOptions map(com.azure.search.documents.implementation.models.AutocompleteOptions obj) {
        if (obj == null) {
            return null;
        }
        AutocompleteOptions autocompleteOptions = new AutocompleteOptions();

        String filter = obj.getFilter();
        autocompleteOptions.setFilter(filter);

        Boolean useFuzzyMatching = obj.isUseFuzzyMatching();
        autocompleteOptions.setUseFuzzyMatching(useFuzzyMatching);

        Double minimumCoverage = obj.getMinimumCoverage();
        autocompleteOptions.setMinimumCoverage(minimumCoverage);

        if (obj.getAutocompleteMode() != null) {
            autocompleteOptions.setAutocompleteMode(obj.getAutocompleteMode());
        }

        Integer top = obj.getTop();
        autocompleteOptions.setTop(top);

        String highlightPostTag = obj.getHighlightPostTag();
        autocompleteOptions.setHighlightPostTag(highlightPostTag);

        if (obj.getSearchFields() != null) {
            List<String> searchFields = new ArrayList<>(obj.getSearchFields());
            AutocompleteOptionsHelper.setSearchFields(autocompleteOptions, searchFields);
        }

        String highlightPreTag = obj.getHighlightPreTag();
        autocompleteOptions.setHighlightPreTag(highlightPreTag);
        return autocompleteOptions;
    }

    private AutocompleteOptionsConverter() {
    }
}
