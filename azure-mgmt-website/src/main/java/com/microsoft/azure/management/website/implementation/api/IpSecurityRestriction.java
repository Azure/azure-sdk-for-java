/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Represents an ip security restriction on a web app.
 */
public class IpSecurityRestriction {
    /**
     * IP address the security restriction is valid for.
     */
    private String ipAddress;

    /**
     * Subnet mask for the range of IP addresses the restriction is valid for.
     */
    private String subnetMask;

    /**
     * Get the ipAddress value.
     *
     * @return the ipAddress value
     */
    public String ipAddress() {
        return this.ipAddress;
    }

    /**
     * Set the ipAddress value.
     *
     * @param ipAddress the ipAddress value to set
     * @return the IpSecurityRestriction object itself.
     */
    public IpSecurityRestriction withIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    /**
     * Get the subnetMask value.
     *
     * @return the subnetMask value
     */
    public String subnetMask() {
        return this.subnetMask;
    }

    /**
     * Set the subnetMask value.
     *
     * @param subnetMask the subnetMask value to set
     * @return the IpSecurityRestriction object itself.
     */
    public IpSecurityRestriction withSubnetMask(String subnetMask) {
        this.subnetMask = subnetMask;
        return this;
    }

}
