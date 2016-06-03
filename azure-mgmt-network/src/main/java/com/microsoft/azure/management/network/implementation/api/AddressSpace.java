/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import java.util.List;

/**
 * AddressSpace contains an array of IP address ranges that can be used by
 * subnets.
 */
public class AddressSpace {
    /**
     * Gets or sets List of address blocks reserved for this virtual network
     * in CIDR notation.
     */
    private List<String> addressPrefixes;

    /**
     * Get the addressPrefixes value.
     *
     * @return the addressPrefixes value
     */
    public List<String> addressPrefixes() {
        return this.addressPrefixes;
    }

    /**
     * Set the addressPrefixes value.
     *
     * @param addressPrefixes the addressPrefixes value to set
     * @return the AddressSpace object itself.
     */
    public AddressSpace withAddressPrefixes(List<String> addressPrefixes) {
        this.addressPrefixes = addressPrefixes;
        return this;
    }

}
