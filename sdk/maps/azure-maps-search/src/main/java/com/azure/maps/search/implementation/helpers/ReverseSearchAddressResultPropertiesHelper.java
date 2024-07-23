// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.maps.search.implementation.helpers;

import com.azure.maps.search.models.ReverseSearchAddressResult;
import com.azure.maps.search.models.ReverseSearchAddressResultItem;
import com.azure.maps.search.models.SearchSummary;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link ReverseSearchAddressResult} instance.
 */
public final class ReverseSearchAddressResultPropertiesHelper {
    private static ReverseSearchAddressResultAccessor accessor;

    public interface ReverseSearchAddressResultAccessor {
        void setSummary(ReverseSearchAddressResult reverseSearchAddressResult, SearchSummary summary);
        void setAddresses(ReverseSearchAddressResult reverseSearchAddressResult,
            List<ReverseSearchAddressResultItem> results);
    }

    public static void setAccessor(final ReverseSearchAddressResultAccessor reverseSearchAddressResultAccessor) {
        accessor = reverseSearchAddressResultAccessor;
    }

    public static void setSummary(ReverseSearchAddressResult reverseSearchAddressResult, SearchSummary summary) {
        accessor.setSummary(reverseSearchAddressResult, summary);
    }

    public static void setAddresses(ReverseSearchAddressResult reverseSearchAddressResult,
        List<ReverseSearchAddressResultItem> results) {
        accessor.setAddresses(reverseSearchAddressResult, results);
    }

    private ReverseSearchAddressResultPropertiesHelper() {
    }
}
