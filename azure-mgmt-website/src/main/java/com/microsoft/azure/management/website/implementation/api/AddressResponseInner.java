/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import java.util.List;

/**
 * Describes main public ip address and any extra vips.
 */
public class AddressResponseInner {
    /**
     * Main public vip.
     */
    private String serviceIpAddress;

    /**
     * VNET internal ip address of the hostingEnvironment (App Service
     * Environment) if it is in internal load-balancing mode.
     */
    private String internalIpAddress;

    /**
     * IP addresses appearing on outbound connections.
     */
    private List<String> outboundIpAddresses;

    /**
     * Additional vips.
     */
    private List<VirtualIPMapping> vipMappings;

    /**
     * Get the serviceIpAddress value.
     *
     * @return the serviceIpAddress value
     */
    public String serviceIpAddress() {
        return this.serviceIpAddress;
    }

    /**
     * Set the serviceIpAddress value.
     *
     * @param serviceIpAddress the serviceIpAddress value to set
     * @return the AddressResponseInner object itself.
     */
    public AddressResponseInner withServiceIpAddress(String serviceIpAddress) {
        this.serviceIpAddress = serviceIpAddress;
        return this;
    }

    /**
     * Get the internalIpAddress value.
     *
     * @return the internalIpAddress value
     */
    public String internalIpAddress() {
        return this.internalIpAddress;
    }

    /**
     * Set the internalIpAddress value.
     *
     * @param internalIpAddress the internalIpAddress value to set
     * @return the AddressResponseInner object itself.
     */
    public AddressResponseInner withInternalIpAddress(String internalIpAddress) {
        this.internalIpAddress = internalIpAddress;
        return this;
    }

    /**
     * Get the outboundIpAddresses value.
     *
     * @return the outboundIpAddresses value
     */
    public List<String> outboundIpAddresses() {
        return this.outboundIpAddresses;
    }

    /**
     * Set the outboundIpAddresses value.
     *
     * @param outboundIpAddresses the outboundIpAddresses value to set
     * @return the AddressResponseInner object itself.
     */
    public AddressResponseInner withOutboundIpAddresses(List<String> outboundIpAddresses) {
        this.outboundIpAddresses = outboundIpAddresses;
        return this;
    }

    /**
     * Get the vipMappings value.
     *
     * @return the vipMappings value
     */
    public List<VirtualIPMapping> vipMappings() {
        return this.vipMappings;
    }

    /**
     * Set the vipMappings value.
     *
     * @param vipMappings the vipMappings value to set
     * @return the AddressResponseInner object itself.
     */
    public AddressResponseInner withVipMappings(List<VirtualIPMapping> vipMappings) {
        this.vipMappings = vipMappings;
        return this;
    }

}
