/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;


/**
 * Response for CheckDnsNameAvailability Api servive call.
 */
public class DnsNameAvailabilityResultInner {
    /**
     * Domain availability (True/False).
     */
    private Boolean available;

    /**
     * Get the available value.
     *
     * @return the available value
     */
    public Boolean available() {
        return this.available;
    }

    /**
     * Set the available value.
     *
     * @param available the available value to set
     * @return the DnsNameAvailabilityResultInner object itself.
     */
    public DnsNameAvailabilityResultInner withAvailable(Boolean available) {
        this.available = available;
        return this;
    }

}
