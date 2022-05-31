// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.maps.search.models;

import java.util.List;

import com.azure.core.annotation.Immutable;
import com.azure.core.models.GeoPosition;
import com.azure.maps.search.implementation.helpers.ReverseSearchAddressResultItemPropertiesHelper;
import com.azure.maps.search.implementation.helpers.Utility;
import com.azure.maps.search.implementation.models.ReverseSearchAddressResultItemPrivate;
import com.fasterxml.jackson.annotation.JsonIgnore;

/** Result object for a Search Address Reverse response. */
@Immutable
public final class ReverseSearchAddressResultItem {
    private Address address;
    @JsonIgnore
    private GeoPosition position;
    private List<RoadUseType> roadUse;
    private MatchType matchType;

    static {
        ReverseSearchAddressResultItemPropertiesHelper.setAccessor(new ReverseSearchAddressResultItemPropertiesHelper
            .ReverseSearchAddressResultItemAccessor() {
            @Override
            public void setFromReverseSearchAddressResultItemPrivate(ReverseSearchAddressResultItem resultItem,
                ReverseSearchAddressResultItemPrivate privateResultItem) {
                resultItem.setFromReverseSearchAddressResultItemPrivate(privateResultItem);
            }
        });
    }

    /**
     * Get the address property: The address of the result.
     *
     * @return the address value.
     */
    public Address getAddress() {
        return this.address;
    }

    /**
     * Get the position property: Position property as GeoPosition.
     *
     * @return the position value.
     */
    public GeoPosition getPosition() {
        return this.position;
    }

    /**
     * Get the roadUse property: The roadUse property.
     *
     * @return the roadUse value.
     */
    public List<RoadUseType> getRoadUse() {
        return this.roadUse;
    }

    /**
     * Get the matchType property: Information on the type of match.
     *
     * <p>One of: * AddressPoint * HouseNumberRange * Street.
     *
     * @return the matchType value.
     */
    public MatchType getMatchType() {
        return this.matchType;
    }

    // private setter
    private void setFromReverseSearchAddressResultItemPrivate(ReverseSearchAddressResultItemPrivate privateItem) {
        this.address = Utility.toAddress(privateItem.getAddress());
        this.roadUse = privateItem.getRoadUse();
        this.matchType = privateItem.getMatchType();

        // position in the internal model is a string separated by commas
        final String position = privateItem.getPosition();
        this.position = Utility.fromCommaSeparatedString(position);
    }
}
