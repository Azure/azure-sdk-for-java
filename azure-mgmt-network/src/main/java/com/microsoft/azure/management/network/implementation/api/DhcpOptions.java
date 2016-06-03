/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import java.util.List;

/**
 * DHCPOptions contains an array of DNS servers available to VMs deployed in
 * the virtual networkStandard DHCP option for a subnet overrides VNET DHCP
 * options.
 */
public class DhcpOptions {
    /**
     * Gets or sets list of DNS servers IP addresses.
     */
    private List<String> dnsServers;

    /**
     * Get the dnsServers value.
     *
     * @return the dnsServers value
     */
    public List<String> dnsServers() {
        return this.dnsServers;
    }

    /**
     * Set the dnsServers value.
     *
     * @param dnsServers the dnsServers value to set
     * @return the DhcpOptions object itself.
     */
    public DhcpOptions withDnsServers(List<String> dnsServers) {
        this.dnsServers = dnsServers;
        return this;
    }

}
