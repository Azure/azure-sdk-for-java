// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.maps.search.models;

import java.util.List;
import java.util.stream.Collectors;

import com.azure.core.annotation.Immutable;
import com.azure.maps.search.implementation.helpers.ReverseSearchCrossStreetAddressResultPropertiesHelper;
import com.azure.maps.search.implementation.helpers.Utility;
import com.azure.maps.search.implementation.models.ReverseSearchCrossStreetAddressResultItemPrivate;
import com.azure.maps.search.implementation.models.SearchSummaryPrivate;

/** This object is returned from a successful Search Address Reverse CrossStreet call. */
@Immutable
public final class ReverseSearchCrossStreetAddressResult {
    private SearchSummary summary;
    private List<ReverseSearchCrossStreetAddressResultItem> addresses;

    static {
        ReverseSearchCrossStreetAddressResultPropertiesHelper.setAccessor(
            new ReverseSearchCrossStreetAddressResultPropertiesHelper
                .ReverseSearchCrossStreetAddressResultAccessor() {
            @Override
            public void setSummary(ReverseSearchCrossStreetAddressResult result, SearchSummaryPrivate privateSearchSummary) {
                result.setSummary(privateSearchSummary);
            }

            @Override
            public void setAddresses(ReverseSearchCrossStreetAddressResult result, List<ReverseSearchCrossStreetAddressResultItemPrivate> privateResults) {
                result.setAddresses(privateResults);
            }
        });
    }

    /**
     * Get the summary property: Summary object for a Search Address Reverse Cross Street response.
     *
     * @return the summary value.
     */
    public SearchSummary getSummary() {
        return this.summary;
    }

    /**
     * Get the addresses property: Addresses array.
     *
     * @return the addresses value.
     */
    public List<ReverseSearchCrossStreetAddressResultItem> getAddresses() {
        return this.addresses;
    }

    // private setters
    private void setSummary(SearchSummaryPrivate privateSearchSummary) {
        this.summary = Utility.toSearchSummary(privateSearchSummary);
    }

    private void setAddresses(List<ReverseSearchCrossStreetAddressResultItemPrivate> privateResults) {
        if (privateResults != null) {
            this.addresses = privateResults.stream()
                .map(item -> Utility.toReverseSearchCrossStreetAddressResultItem(item))
                .collect(Collectors.toList());
        }
    }
}
