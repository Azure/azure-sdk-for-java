package com.azure.maps.search.implementation.helpers;

import java.util.List;

import com.azure.maps.search.implementation.models.ReverseSearchCrossStreetAddressResultItemPrivate;
import com.azure.maps.search.implementation.models.SearchSummaryPrivate;
import com.azure.maps.search.models.ReverseSearchCrossStreetAddressResult;

/**
 * The helper class to set the non-public properties of an {@link ReverseSearchCrossStreetAddressResult} instance.
 */
public final class ReverseSearchCrossStreetAddressResultPropertiesHelper {
    private static ReverseSearchCrossStreetAddressResultAccessor accessor;

    private ReverseSearchCrossStreetAddressResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ReverseSearchCrossStreetAddressResult} instance.
     */
    public interface ReverseSearchCrossStreetAddressResultAccessor {
        void setSummary(ReverseSearchCrossStreetAddressResult result, SearchSummaryPrivate privateSearchSummary);
        void setAddresses(ReverseSearchCrossStreetAddressResult result, List<ReverseSearchCrossStreetAddressResultItemPrivate> privateResults);
    }

    /**
     * The method called from {@link ReverseSearchCrossStreetAddressResult} to set it's accessor.
     *
     * @param ReverseSearchCrossStreetAddressResultAccessor The accessor.
     */
    public static void setAccessor(final ReverseSearchCrossStreetAddressResultAccessor ReverseSearchCrossStreetAddressResultAccessor) {
        accessor = ReverseSearchCrossStreetAddressResultAccessor;
    }

    /**
     * Sets the search summary of this {@link ReverseSearchCrossStreetAddressResult}
     *
     * @param result
     * @param privateSearchSummary
     */
    public static void setSummary(ReverseSearchCrossStreetAddressResult result, SearchSummaryPrivate privateSearchSummary) {
        accessor.setSummary(result, privateSearchSummary);
    }

    /**
     * Sets the addresses of this {@link ReverseSearchCrossStreetAddressResult}
     *
     * @param result
     * @param privateResults
     */
    public static void setAddresses(ReverseSearchCrossStreetAddressResult result, List<ReverseSearchCrossStreetAddressResultItemPrivate> privateResults) {
        accessor.setAddresses(result, privateResults);
    }
}

