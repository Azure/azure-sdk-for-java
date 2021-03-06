/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.network.v2020_08_01;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SKU of a public IP address.
 */
public class PublicIPAddressSku {
    /**
     * Name of a public IP address SKU. Possible values include: 'Basic',
     * 'Standard'.
     */
    @JsonProperty(value = "name")
    private PublicIPAddressSkuName name;

    /**
     * Tier of a public IP address SKU. Possible values include: 'Regional',
     * 'Global'.
     */
    @JsonProperty(value = "tier")
    private PublicIPAddressSkuTier tier;

    /**
     * Get name of a public IP address SKU. Possible values include: 'Basic', 'Standard'.
     *
     * @return the name value
     */
    public PublicIPAddressSkuName name() {
        return this.name;
    }

    /**
     * Set name of a public IP address SKU. Possible values include: 'Basic', 'Standard'.
     *
     * @param name the name value to set
     * @return the PublicIPAddressSku object itself.
     */
    public PublicIPAddressSku withName(PublicIPAddressSkuName name) {
        this.name = name;
        return this;
    }

    /**
     * Get tier of a public IP address SKU. Possible values include: 'Regional', 'Global'.
     *
     * @return the tier value
     */
    public PublicIPAddressSkuTier tier() {
        return this.tier;
    }

    /**
     * Set tier of a public IP address SKU. Possible values include: 'Regional', 'Global'.
     *
     * @param tier the tier value to set
     * @return the PublicIPAddressSku object itself.
     */
    public PublicIPAddressSku withTier(PublicIPAddressSkuTier tier) {
        this.tier = tier;
        return this;
    }

}
