// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.models.AutocompleteItem;
import com.azure.search.documents.models.AutocompleteResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.AutocompleteResult} and
 * {@link AutocompleteResult}.
 */
public final class AutocompleteResultConverter {
    /**
     * Maps from {@link com.azure.search.documents.implementation.models.AutocompleteResult} to
     * {@link AutocompleteResult}.
     */
    public static AutocompleteResult map(com.azure.search.documents.implementation.models.AutocompleteResult obj) {
        if (obj == null) {
            return null;
        }

        List<AutocompleteItem> results =
            obj.getResults().stream().map(AutocompleteItemConverter::map).collect(Collectors.toList());
        AutocompleteResult autocompleteResult = new AutocompleteResult(results);

        Double coverage = obj.getCoverage();
        PrivateFieldAccessHelper.set(autocompleteResult, "coverage", coverage);

        return autocompleteResult;
    }

    /**
     * Maps from {@link AutocompleteResult} to
     * {@link com.azure.search.documents.implementation.models.AutocompleteResult}.
     */
    public static com.azure.search.documents.implementation.models.AutocompleteResult map(AutocompleteResult obj) {
        if (obj == null) {
            return null;
        }

        List<com.azure.search.documents.implementation.models.AutocompleteItem> results = obj.getResults() == null ?
            null : obj.getResults().stream().map(AutocompleteItemConverter::map).collect(Collectors.toList());
        com.azure.search.documents.implementation.models.AutocompleteResult autocompleteResult =
            new com.azure.search.documents.implementation.models.AutocompleteResult(results);

        Double coverage = obj.getCoverage();
        PrivateFieldAccessHelper.set(autocompleteResult, "coverage", coverage);

        autocompleteResult.validate();
        return autocompleteResult;
    }

    private AutocompleteResultConverter() {
    }
}
