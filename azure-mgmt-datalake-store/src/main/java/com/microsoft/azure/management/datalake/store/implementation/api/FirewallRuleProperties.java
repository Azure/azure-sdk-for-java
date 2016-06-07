/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.store.implementation.api;


/**
 * Data Lake Store firewall rule properties information.
 */
public class FirewallRuleProperties {
    /**
     * Gets or sets the start IP address for the firewall rule.
     */
    private String startIpAddress;

    /**
     * Gets or sets the end IP address for the firewall rule.
     */
    private String endIpAddress;

    /**
     * Get the startIpAddress value.
     *
     * @return the startIpAddress value
     */
    public String startIpAddress() {
        return this.startIpAddress;
    }

    /**
     * Set the startIpAddress value.
     *
     * @param startIpAddress the startIpAddress value to set
     * @return the FirewallRuleProperties object itself.
     */
    public FirewallRuleProperties withStartIpAddress(String startIpAddress) {
        this.startIpAddress = startIpAddress;
        return this;
    }

    /**
     * Get the endIpAddress value.
     *
     * @return the endIpAddress value
     */
    public String endIpAddress() {
        return this.endIpAddress;
    }

    /**
     * Set the endIpAddress value.
     *
     * @param endIpAddress the endIpAddress value to set
     * @return the FirewallRuleProperties object itself.
     */
    public FirewallRuleProperties withEndIpAddress(String endIpAddress) {
        this.endIpAddress = endIpAddress;
        return this;
    }

}
