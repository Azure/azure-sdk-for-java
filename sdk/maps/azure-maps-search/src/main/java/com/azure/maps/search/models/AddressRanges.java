// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.maps.search.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.models.GeoPosition;
import com.azure.maps.search.implementation.helpers.AddressRangesPropertiesHelper;
import com.azure.maps.search.implementation.models.AddressRangesPrivate;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Describes the address range on both sides of the street for a search result. Coordinates for the start and end
 * locations of the address range are included.
 */
@Immutable
public final class AddressRanges {
    private String rangeLeft;
    private String rangeRight;
    @JsonIgnore
    private GeoPosition from;
    @JsonIgnore
    private GeoPosition to;

    static {
        AddressRangesPropertiesHelper.setAccessor(new AddressRangesPropertiesHelper.AddressRangesAccessor() {
            @Override
            public void setFromAddressRangesPrivate(AddressRanges addressRanges, AddressRangesPrivate addressRangesPrivate) {
                addressRanges.setFromAddressRangesPrivate(addressRangesPrivate);
            }
        });
    }

    /**
     * Get the rangeLeft property: Address range on the left side of the street.
     *
     * @return the rangeLeft value.
     */
    public String getRangeLeft() {
        return this.rangeLeft;
    }

    /**
     * Get the rangeRight property: Address range on the right side of the street.
     *
     * @return the rangeRight value.
     */
    public String getRangeRight() {
        return this.rangeRight;
    }

    /**
     * Get the from property: A location represented as a latitude and longitude using short names 'lat' &amp; 'lon'.
     *
     * @return the from value.
     */
    public GeoPosition getFrom() {
        return this.from;
    }

    /**
     * Get the to property: A location represented as a latitude and longitude using short names 'lat' &amp; 'lon'.
     *
     * @return the to value.
     */
    public GeoPosition getTo() {
        return this.to;
    }

    // private setter for properties that need transformation
    private void setFromAddressRangesPrivate(AddressRangesPrivate addressRangesPrivate) {
        this.rangeLeft = addressRangesPrivate.getRangeLeft();
        this.rangeRight = addressRangesPrivate.getRangeRight();
        this.from = new GeoPosition(addressRangesPrivate.getFrom().getLon(), addressRangesPrivate.getFrom().getLat());
        this.to = new GeoPosition(addressRangesPrivate.getTo().getLon(), addressRangesPrivate.getTo().getLat());
    }
}
