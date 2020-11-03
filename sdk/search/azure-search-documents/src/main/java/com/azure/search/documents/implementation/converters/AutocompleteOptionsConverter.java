// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

<<<<<<< HEAD
import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.models.AutocompleteMode;
=======
>>>>>>> bfd056a1647f7232e7d7cb82ca2a5ad85b9bb6ec
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
            AutocompleteMode autocompleteMode = AutocompleteModeConverter.map(obj.getAutocompleteMode());
            autocompleteOptions.setAutocompleteMode(autocompleteMode);
        }

        Integer top = obj.getTop();
        autocompleteOptions.setTop(top);

        String highlightPostTag = obj.getHighlightPostTag();
        autocompleteOptions.setHighlightPostTag(highlightPostTag);

        if (obj.getSearchFields() != null) {
            List<String> searchFields = new ArrayList<>(obj.getSearchFields());
<<<<<<< HEAD
            PrivateFieldAccessHelper.set(autocompleteOptions, "searchFields", searchFields);
        }

        String highlightPreTag = obj.getHighlightPreTag();
        autocompleteOptions.setHighlightPreTag(highlightPreTag);
        return autocompleteOptions;
    }

    /**
     * Maps from {@link AutocompleteOptions} to
     * {@link com.azure.search.documents.implementation.models.AutocompleteOptions}.
     */
    public static com.azure.search.documents.implementation.models.AutocompleteOptions map(AutocompleteOptions obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.AutocompleteOptions autocompleteOptions =
            new com.azure.search.documents.implementation.models.AutocompleteOptions();

        String filter = obj.getFilter();
        autocompleteOptions.setFilter(filter);

        Boolean useFuzzyMatching = obj.useFuzzyMatching();
        autocompleteOptions.setUseFuzzyMatching(useFuzzyMatching);

        Double minimumCoverage = obj.getMinimumCoverage();
        autocompleteOptions.setMinimumCoverage(minimumCoverage);

        if (obj.getAutocompleteMode() != null) {
            com.azure.search.documents.implementation.models.AutocompleteMode autocompleteMode =
                AutocompleteModeConverter.map(obj.getAutocompleteMode());
            autocompleteOptions.setAutocompleteMode(autocompleteMode);
        }

        Integer top = obj.getTop();
        autocompleteOptions.setTop(top);

        String highlightPostTag = obj.getHighlightPostTag();
        autocompleteOptions.setHighlightPostTag(highlightPostTag);

        if (obj.getSearchFields() != null) {
            List<String> searchFields = new ArrayList<>(obj.getSearchFields());
            PrivateFieldAccessHelper.set(autocompleteOptions, "searchFields", searchFields);
=======
            AutocompleteOptionsHelper.setSearchFields(autocompleteOptions, searchFields);
>>>>>>> bfd056a1647f7232e7d7cb82ca2a5ad85b9bb6ec
        }

        String highlightPreTag = obj.getHighlightPreTag();
        autocompleteOptions.setHighlightPreTag(highlightPreTag);
        return autocompleteOptions;
    }

    private AutocompleteOptionsConverter() {
    }
}
