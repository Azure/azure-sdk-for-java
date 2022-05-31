// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.maps.search.models;

import java.util.List;
import java.util.stream.Collectors;

import com.azure.core.annotation.Immutable;
import com.azure.maps.search.implementation.helpers.SearchAddressResultPropertiesHelper;
import com.azure.maps.search.implementation.helpers.Utility;
import com.azure.maps.search.implementation.models.SearchAddressResultItemPrivate;
import com.azure.maps.search.implementation.models.SearchSummaryPrivate;

/** This object is returned from a successful Search calls. */
@Immutable
public final class SearchAddressResult {
    private SearchSummary summary;
    private List<SearchAddressResultItem> results;

    static {
        SearchAddressResultPropertiesHelper.setAccessor(new SearchAddressResultPropertiesHelper
            .SearchAddressResultAccessor() {
            @Override
            public void setSummary(SearchAddressResult result, SearchSummaryPrivate privateSearchSummary) {
                result.setSummary(privateSearchSummary);
            }

            @Override
            public void setResults(SearchAddressResult result,
                List<SearchAddressResultItemPrivate> privateResults) {
                result.setResults(privateResults);
            }
        });
    }

    /**
     * Get the summary property: Summary object for a Search API response.
     *
     * @return the summary value.
     */
    public SearchSummary getSummary() {
        return this.summary;
    }

    /**
     * Get the results property: A list of Search API results.
     *
     * @return the results value.
     */
    public List<SearchAddressResultItem> getResults() {
        return this.results;
    }

    // private setters
    private void setSummary(SearchSummaryPrivate privateSearchSummary) {
        this.summary = Utility.toSearchSummary(privateSearchSummary);
    }

    private void setResults(List<SearchAddressResultItemPrivate> privateResults) {
        if (privateResults != null) {
            this.results = privateResults.stream()
                .map(item -> Utility.toSearchAddressResultItem(item))
                .collect(Collectors.toList());
        }
    }
}
