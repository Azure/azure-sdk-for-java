/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import java.util.List;

/**
 * Dns Settings of a network interface.
 */
public class NetworkInterfaceDnsSettings {
    /**
     * Gets or sets list of DNS servers IP addresses.
     */
    private List<String> dnsServers;

    /**
     * Gets or sets list of Applied DNS servers IP addresses.
     */
    private List<String> appliedDnsServers;

    /**
     * Gets or sets the Internal DNS name.
     */
    private String internalDnsNameLabel;

    /**
     * Gets or sets full IDNS name in the form,
     * DnsName.VnetId.ZoneId.TopleveSuffix. This is set when the NIC is
     * associated to a VM.
     */
    private String internalFqdn;

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
     * @return the NetworkInterfaceDnsSettings object itself.
     */
    public NetworkInterfaceDnsSettings withDnsServers(List<String> dnsServers) {
        this.dnsServers = dnsServers;
        return this;
    }

    /**
     * Get the appliedDnsServers value.
     *
     * @return the appliedDnsServers value
     */
    public List<String> appliedDnsServers() {
        return this.appliedDnsServers;
    }

    /**
     * Set the appliedDnsServers value.
     *
     * @param appliedDnsServers the appliedDnsServers value to set
     * @return the NetworkInterfaceDnsSettings object itself.
     */
    public NetworkInterfaceDnsSettings withAppliedDnsServers(List<String> appliedDnsServers) {
        this.appliedDnsServers = appliedDnsServers;
        return this;
    }

    /**
     * Get the internalDnsNameLabel value.
     *
     * @return the internalDnsNameLabel value
     */
    public String internalDnsNameLabel() {
        return this.internalDnsNameLabel;
    }

    /**
     * Set the internalDnsNameLabel value.
     *
     * @param internalDnsNameLabel the internalDnsNameLabel value to set
     * @return the NetworkInterfaceDnsSettings object itself.
     */
    public NetworkInterfaceDnsSettings withInternalDnsNameLabel(String internalDnsNameLabel) {
        this.internalDnsNameLabel = internalDnsNameLabel;
        return this;
    }

    /**
     * Get the internalFqdn value.
     *
     * @return the internalFqdn value
     */
    public String internalFqdn() {
        return this.internalFqdn;
    }

    /**
     * Set the internalFqdn value.
     *
     * @param internalFqdn the internalFqdn value to set
     * @return the NetworkInterfaceDnsSettings object itself.
     */
    public NetworkInterfaceDnsSettings withInternalFqdn(String internalFqdn) {
        this.internalFqdn = internalFqdn;
        return this;
    }

}
