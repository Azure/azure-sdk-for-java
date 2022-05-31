// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.maps.search.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.models.GeoPosition;
import com.azure.maps.search.implementation.helpers.ReverseSearchCrossStreetAddressResultItemPropertiesHelper;
import com.azure.maps.search.implementation.helpers.Utility;
import com.azure.maps.search.implementation.models.ReverseSearchCrossStreetAddressResultItemPrivate;

/** Result object for a Search Address Reverse Cross Street response. */
@Immutable
public final class ReverseSearchCrossStreetAddressResultItem {
    private Address address;
    private GeoPosition position;

    static {
        ReverseSearchCrossStreetAddressResultItemPropertiesHelper.setAccessor(
                new ReverseSearchCrossStreetAddressResultItemPropertiesHelper
                    .ReverseSearchCrossStreetAddressResultItemAccessor() {
            @Override
            public void setFromReverseSearchCrossStreetAddressResultItemPrivate(
                    ReverseSearchCrossStreetAddressResultItem resultItem,
                    ReverseSearchCrossStreetAddressResultItemPrivate privateResultItem) {
                resultItem.setFromReverseSearchCrossStreetAddressResultItemPrivate(privateResultItem);
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

    // private setter
    private void setFromReverseSearchCrossStreetAddressResultItemPrivate(
            ReverseSearchCrossStreetAddressResultItemPrivate privateItem) {
        this.address = Utility.toAddress(privateItem.getAddress());

        // position in the internal model is a string separated by commas
        final String position = privateItem.getPosition();
        this.position = Utility.fromCommaSeparatedString(position);
    }
}
