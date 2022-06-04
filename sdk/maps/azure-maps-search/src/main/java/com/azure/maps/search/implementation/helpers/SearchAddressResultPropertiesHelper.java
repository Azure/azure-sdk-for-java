package com.azure.maps.search.implementation.helpers;

import java.util.List;

import com.azure.maps.search.models.SearchSummary;
import com.azure.maps.search.models.SearchAddressResult;
import com.azure.maps.search.models.SearchAddressResultItem;

/**
 * The helper class to set the non-public properties of an {@link SearchAddressResult} instance.
 */
public final class SearchAddressResultPropertiesHelper {
    private static SearchAddressResultAccessor accessor;

    private SearchAddressResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link SearchAddressResult} instance.
     */
    public interface SearchAddressResultAccessor {
        void setSummary(SearchAddressResult result, SearchSummary privateSearchSummary);
        void setResults(SearchAddressResult result, List<SearchAddressResultItem> privateResults);
    }

    /**
     * The method called from {@link SearchAddressResult} to set it's accessor.
     *
     * @param searchAddressResultAccessor The accessor.
     */
    public static void setAccessor(final SearchAddressResultAccessor searchAddressResultAccessor) {
        accessor = searchAddressResultAccessor;
    }

    /**
     * Sets the search summary of this {@link SearchAddressResult} instance from a private model.
     *
     * @param result
     * @param privateSearchSummary
     */
    public static void setSummary(SearchAddressResult result, SearchSummary privateSearchSummary) {
        accessor.setSummary(result, privateSearchSummary);
    }

    /**
     * Sets the results of this {@link SearchAddressResult} instance from a private model.
     *
     * @param result
     * @param privateResults
     */
    public static void setResults(SearchAddressResult result, List<SearchAddressResultItem> privateResults) {
        accessor.setResults(result, privateResults);
    }
}

