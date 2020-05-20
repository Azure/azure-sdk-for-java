// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.models.AutocompleteMode;
import com.azure.search.documents.models.AutocompleteOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.AutocompleteOptions} and
 * {@link AutocompleteOptions}.
 */
public final class AutocompleteOptionsConverter {
    private static final ClientLogger LOGGER = new ClientLogger(AutocompleteOptionsConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.AutocompleteOptions} to
     * {@link AutocompleteOptions}.
     */
    public static AutocompleteOptions map(com.azure.search.documents.implementation.models.AutocompleteOptions obj) {
        if (obj == null) {
            return null;
        }
        AutocompleteOptions autocompleteOptions = new AutocompleteOptions();

        String _filter = obj.getFilter();
        autocompleteOptions.setFilter(_filter);

        Boolean _useFuzzyMatching = obj.isUseFuzzyMatching();
        autocompleteOptions.setUseFuzzyMatching(_useFuzzyMatching);

        Double _minimumCoverage = obj.getMinimumCoverage();
        autocompleteOptions.setMinimumCoverage(_minimumCoverage);

        if (obj.getAutocompleteMode() != null) {
            AutocompleteMode _autocompleteMode = AutocompleteModeConverter.map(obj.getAutocompleteMode());
            autocompleteOptions.setAutocompleteMode(_autocompleteMode);
        }

        Integer _top = obj.getTop();
        autocompleteOptions.setTop(_top);

        String _highlightPostTag = obj.getHighlightPostTag();
        autocompleteOptions.setHighlightPostTag(_highlightPostTag);

        if (obj.getSearchFields() != null) {
            List<String> _searchFields = new ArrayList<>(obj.getSearchFields());
            PrivateFieldAccessHelper.set(autocompleteOptions, "searchFields", _searchFields);
        }

        String _highlightPreTag = obj.getHighlightPreTag();
        autocompleteOptions.setHighlightPreTag(_highlightPreTag);
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

        String _filter = obj.getFilter();
        autocompleteOptions.setFilter(_filter);

        Boolean _useFuzzyMatching = obj.useFuzzyMatching();
        autocompleteOptions.setUseFuzzyMatching(_useFuzzyMatching);

        Double _minimumCoverage = obj.getMinimumCoverage();
        autocompleteOptions.setMinimumCoverage(_minimumCoverage);

        if (obj.getAutocompleteMode() != null) {
            com.azure.search.documents.implementation.models.AutocompleteMode _autocompleteMode =
                AutocompleteModeConverter.map(obj.getAutocompleteMode());
            autocompleteOptions.setAutocompleteMode(_autocompleteMode);
        }

        Integer _top = obj.getTop();
        autocompleteOptions.setTop(_top);

        String _highlightPostTag = obj.getHighlightPostTag();
        autocompleteOptions.setHighlightPostTag(_highlightPostTag);

        if (obj.getSearchFields() != null) {
            List<String> _searchFields = new ArrayList<>(obj.getSearchFields());
            PrivateFieldAccessHelper.set(autocompleteOptions, "searchFields", _searchFields);
        }

        String _highlightPreTag = obj.getHighlightPreTag();
        autocompleteOptions.setHighlightPreTag(_highlightPreTag);
        return autocompleteOptions;
    }
}
