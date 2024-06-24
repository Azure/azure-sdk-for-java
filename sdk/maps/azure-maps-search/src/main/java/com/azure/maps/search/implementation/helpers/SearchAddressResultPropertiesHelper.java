// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.maps.search.implementation.helpers;

import com.azure.maps.search.models.SearchAddressResult;
import com.azure.maps.search.models.SearchAddressResultItem;
import com.azure.maps.search.models.SearchSummary;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link SearchAddressResult} instance.
 */
public final class SearchAddressResultPropertiesHelper {
    private static SearchAddressResultAccessor accessor;

    public interface SearchAddressResultAccessor {
        void setSummary(SearchAddressResult searchAddressResult, SearchSummary summary);
        void setResults(SearchAddressResult searchAddressResult, List<SearchAddressResultItem> results);
    }

    public static void setAccessor(final SearchAddressResultAccessor searchAddressResultAccessor) {
        accessor = searchAddressResultAccessor;
    }

    public static void setSummary(SearchAddressResult searchAddressResult, SearchSummary summary) {
        accessor.setSummary(searchAddressResult, summary);
    }

    public static void setResults(SearchAddressResult searchAddressResult, List<SearchAddressResultItem> results) {
        accessor.setResults(searchAddressResult, results);
    }

    private SearchAddressResultPropertiesHelper() {
    }
}
