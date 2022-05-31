package com.azure.maps.search.implementation.helpers;

import com.azure.maps.search.implementation.models.SearchSummaryPrivate;
import com.azure.maps.search.models.SearchSummary;

/**
 * The helper class to set the non-public properties of an {@link SearchSummary} instance.
 */
public final class SearchSummaryPropertiesHelper {
    private static SearchSummaryAccessor accessor;

    private SearchSummaryPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link SearchSummary} instance.
     */
    public interface SearchSummaryAccessor {
        void setFromPrivateSearchSummary(SearchSummary searchSummary, SearchSummaryPrivate privateSearchSummary);
    }

    /**
     * The method called from {@link SearchSummary} to set it's accessor.
     *
     * @param searchAddressResultAccessor The accessor.
     */
    public static void setAccessor(final SearchSummaryAccessor searchSummaryAccessor) {
        accessor = searchSummaryAccessor;
    }

    /**
     * Sets all properties of this {@link SearchSummary} instance from a private model.
     *
     * @param searchSummary
     * @param privateSearchSummary
     */
    public static void setFromPrivateSearchSummary(SearchSummary searchSummary, SearchSummaryPrivate privateSearchSummary) {
        accessor.setFromPrivateSearchSummary(searchSummary, privateSearchSummary);
    }
}

