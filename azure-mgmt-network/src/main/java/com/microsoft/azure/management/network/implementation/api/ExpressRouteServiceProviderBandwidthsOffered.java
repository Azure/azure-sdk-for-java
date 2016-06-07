/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;


/**
 * Contains Bandwidths offered in ExpressRouteServiceProviders.
 */
public class ExpressRouteServiceProviderBandwidthsOffered {
    /**
     * Gets the OfferName.
     */
    private String offerName;

    /**
     * Gets the ValueInMbps.
     */
    private Integer valueInMbps;

    /**
     * Get the offerName value.
     *
     * @return the offerName value
     */
    public String offerName() {
        return this.offerName;
    }

    /**
     * Set the offerName value.
     *
     * @param offerName the offerName value to set
     * @return the ExpressRouteServiceProviderBandwidthsOffered object itself.
     */
    public ExpressRouteServiceProviderBandwidthsOffered withOfferName(String offerName) {
        this.offerName = offerName;
        return this;
    }

    /**
     * Get the valueInMbps value.
     *
     * @return the valueInMbps value
     */
    public Integer valueInMbps() {
        return this.valueInMbps;
    }

    /**
     * Set the valueInMbps value.
     *
     * @param valueInMbps the valueInMbps value to set
     * @return the ExpressRouteServiceProviderBandwidthsOffered object itself.
     */
    public ExpressRouteServiceProviderBandwidthsOffered withValueInMbps(Integer valueInMbps) {
        this.valueInMbps = valueInMbps;
        return this;
    }

}
