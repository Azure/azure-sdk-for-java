/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.models;


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
    public String getIpAddress() {
        return this.ipAddress;
    }

    /**
     * Set the ipAddress value.
     *
     * @param ipAddress the ipAddress value to set
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Get the subnetMask value.
     *
     * @return the subnetMask value
     */
    public String getSubnetMask() {
        return this.subnetMask;
    }

    /**
     * Set the subnetMask value.
     *
     * @param subnetMask the subnetMask value to set
     */
    public void setSubnetMask(String subnetMask) {
        this.subnetMask = subnetMask;
    }

}
