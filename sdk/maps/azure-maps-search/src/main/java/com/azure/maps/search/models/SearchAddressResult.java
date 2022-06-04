// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.maps.search.models;

import java.util.List;
import java.util.stream.Collectors;

import com.azure.core.annotation.Immutable;
import com.azure.maps.search.implementation.helpers.SearchAddressResultPropertiesHelper;
import com.azure.maps.search.implementation.helpers.Utility;
import com.azure.maps.search.models.SearchSummary;

/** This object is returned from a successful Search calls. */
@Immutable
public final class SearchAddressResult {
    private SearchSummary summary;
    private List<SearchAddressResultItem> results;

    static {
        SearchAddressResultPropertiesHelper.setAccessor(new SearchAddressResultPropertiesHelper
            .SearchAddressResultAccessor() {
            @Override
            public void setSummary(SearchAddressResult result, SearchSummary privateSearchSummary) {
                result.setSummary(privateSearchSummary);
            }

            @Override
            public void setResults(SearchAddressResult result,
                List<SearchAddressResultItem> privateResults) {
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
    private void setSummary(SearchSummary privateSearchSummary) {
        this.summary = privateSearchSummary;
    }

    private void setResults(List<SearchAddressResultItem> privateResults) {
        this.results = privateResults;
    }
}
