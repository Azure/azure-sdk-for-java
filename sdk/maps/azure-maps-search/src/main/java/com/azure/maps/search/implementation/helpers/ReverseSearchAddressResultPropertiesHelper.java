package com.azure.maps.search.implementation.helpers;

import java.util.List;

import com.azure.maps.search.implementation.models.ReverseSearchAddressResultItemPrivate;
import com.azure.maps.search.implementation.models.SearchSummaryPrivate;
import com.azure.maps.search.models.ReverseSearchAddressResult;

/**
 * The helper class to set the non-public properties of an {@link ReverseSearchAddressResult} instance.
 */
public final class ReverseSearchAddressResultPropertiesHelper {
    private static ReverseSearchAddressResultAccessor accessor;

    private ReverseSearchAddressResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ReverseSearchAddressResult} instance.
     */
    public interface ReverseSearchAddressResultAccessor {
        void setSummary(ReverseSearchAddressResult result, SearchSummaryPrivate privateSearchSummary);
        void setAddresses(ReverseSearchAddressResult result, List<ReverseSearchAddressResultItemPrivate> privateResults);
    }

    /**
     * The method called from {@link ReverseSearchAddressResult} to set it's accessor.
     *
     * @param ReverseSearchAddressResultAccessor The accessor.
     */
    public static void setAccessor(final ReverseSearchAddressResultAccessor reverseSearchAddressResultAccessor) {
        accessor = reverseSearchAddressResultAccessor;
    }

    /**
     * Sets the search summary of this {@link ReverseSearchAddressResult}
     *
     * @param result
     * @param privateSearchSummary
     */
    public static void setSummary(ReverseSearchAddressResult result, SearchSummaryPrivate privateSearchSummary) {
        accessor.setSummary(result, privateSearchSummary);
    }

    /**
     * Sets the addresses of this {@link ReverseSearchAddressResult}
     *
     * @param result
     * @param privateResults
     */
    public static void setAddresses(ReverseSearchAddressResult result, List<ReverseSearchAddressResultItemPrivate> privateResults) {
        accessor.setAddresses(result, privateResults);
    }
}

