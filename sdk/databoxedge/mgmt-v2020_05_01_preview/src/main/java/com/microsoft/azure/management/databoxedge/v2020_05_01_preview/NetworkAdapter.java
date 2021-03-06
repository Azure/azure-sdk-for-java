/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.databoxedge.v2020_05_01_preview;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the networkAdapter on a device.
 */
public class NetworkAdapter {
    /**
     * Instance ID of network adapter.
     */
    @JsonProperty(value = "adapterId", access = JsonProperty.Access.WRITE_ONLY)
    private String adapterId;

    /**
     * Hardware position of network adapter.
     */
    @JsonProperty(value = "adapterPosition", access = JsonProperty.Access.WRITE_ONLY)
    private NetworkAdapterPosition adapterPosition;

    /**
     * Logical index of the adapter.
     */
    @JsonProperty(value = "index", access = JsonProperty.Access.WRITE_ONLY)
    private Integer index;

    /**
     * Node ID of the network adapter.
     */
    @JsonProperty(value = "nodeId", access = JsonProperty.Access.WRITE_ONLY)
    private String nodeId;

    /**
     * Network adapter name.
     */
    @JsonProperty(value = "networkAdapterName", access = JsonProperty.Access.WRITE_ONLY)
    private String networkAdapterName;

    /**
     * Hardware label for the adapter.
     */
    @JsonProperty(value = "label", access = JsonProperty.Access.WRITE_ONLY)
    private String label;

    /**
     * MAC address.
     */
    @JsonProperty(value = "macAddress", access = JsonProperty.Access.WRITE_ONLY)
    private String macAddress;

    /**
     * Link speed.
     */
    @JsonProperty(value = "linkSpeed", access = JsonProperty.Access.WRITE_ONLY)
    private Long linkSpeed;

    /**
     * Value indicating whether this adapter is valid. Possible values include:
     * 'Inactive', 'Active'.
     */
    @JsonProperty(value = "status", access = JsonProperty.Access.WRITE_ONLY)
    private NetworkAdapterStatus status;

    /**
     * Value indicating whether this adapter is RDMA capable. Possible values
     * include: 'Incapable', 'Capable'.
     */
    @JsonProperty(value = "rdmaStatus")
    private NetworkAdapterRDMAStatus rdmaStatus;

    /**
     * Value indicating whether this adapter has DHCP enabled. Possible values
     * include: 'Disabled', 'Enabled'.
     */
    @JsonProperty(value = "dhcpStatus")
    private NetworkAdapterDHCPStatus dhcpStatus;

    /**
     * The IPv4 configuration of the network adapter.
     */
    @JsonProperty(value = "ipv4Configuration", access = JsonProperty.Access.WRITE_ONLY)
    private Ipv4Config ipv4Configuration;

    /**
     * The IPv6 configuration of the network adapter.
     */
    @JsonProperty(value = "ipv6Configuration", access = JsonProperty.Access.WRITE_ONLY)
    private Ipv6Config ipv6Configuration;

    /**
     * The IPv6 local address.
     */
    @JsonProperty(value = "ipv6LinkLocalAddress", access = JsonProperty.Access.WRITE_ONLY)
    private String ipv6LinkLocalAddress;

    /**
     * The list of DNS Servers of the device.
     */
    @JsonProperty(value = "dnsServers", access = JsonProperty.Access.WRITE_ONLY)
    private List<String> dnsServers;

    /**
     * Get instance ID of network adapter.
     *
     * @return the adapterId value
     */
    public String adapterId() {
        return this.adapterId;
    }

    /**
     * Get hardware position of network adapter.
     *
     * @return the adapterPosition value
     */
    public NetworkAdapterPosition adapterPosition() {
        return this.adapterPosition;
    }

    /**
     * Get logical index of the adapter.
     *
     * @return the index value
     */
    public Integer index() {
        return this.index;
    }

    /**
     * Get node ID of the network adapter.
     *
     * @return the nodeId value
     */
    public String nodeId() {
        return this.nodeId;
    }

    /**
     * Get network adapter name.
     *
     * @return the networkAdapterName value
     */
    public String networkAdapterName() {
        return this.networkAdapterName;
    }

    /**
     * Get hardware label for the adapter.
     *
     * @return the label value
     */
    public String label() {
        return this.label;
    }

    /**
     * Get mAC address.
     *
     * @return the macAddress value
     */
    public String macAddress() {
        return this.macAddress;
    }

    /**
     * Get link speed.
     *
     * @return the linkSpeed value
     */
    public Long linkSpeed() {
        return this.linkSpeed;
    }

    /**
     * Get value indicating whether this adapter is valid. Possible values include: 'Inactive', 'Active'.
     *
     * @return the status value
     */
    public NetworkAdapterStatus status() {
        return this.status;
    }

    /**
     * Get value indicating whether this adapter is RDMA capable. Possible values include: 'Incapable', 'Capable'.
     *
     * @return the rdmaStatus value
     */
    public NetworkAdapterRDMAStatus rdmaStatus() {
        return this.rdmaStatus;
    }

    /**
     * Set value indicating whether this adapter is RDMA capable. Possible values include: 'Incapable', 'Capable'.
     *
     * @param rdmaStatus the rdmaStatus value to set
     * @return the NetworkAdapter object itself.
     */
    public NetworkAdapter withRdmaStatus(NetworkAdapterRDMAStatus rdmaStatus) {
        this.rdmaStatus = rdmaStatus;
        return this;
    }

    /**
     * Get value indicating whether this adapter has DHCP enabled. Possible values include: 'Disabled', 'Enabled'.
     *
     * @return the dhcpStatus value
     */
    public NetworkAdapterDHCPStatus dhcpStatus() {
        return this.dhcpStatus;
    }

    /**
     * Set value indicating whether this adapter has DHCP enabled. Possible values include: 'Disabled', 'Enabled'.
     *
     * @param dhcpStatus the dhcpStatus value to set
     * @return the NetworkAdapter object itself.
     */
    public NetworkAdapter withDhcpStatus(NetworkAdapterDHCPStatus dhcpStatus) {
        this.dhcpStatus = dhcpStatus;
        return this;
    }

    /**
     * Get the IPv4 configuration of the network adapter.
     *
     * @return the ipv4Configuration value
     */
    public Ipv4Config ipv4Configuration() {
        return this.ipv4Configuration;
    }

    /**
     * Get the IPv6 configuration of the network adapter.
     *
     * @return the ipv6Configuration value
     */
    public Ipv6Config ipv6Configuration() {
        return this.ipv6Configuration;
    }

    /**
     * Get the IPv6 local address.
     *
     * @return the ipv6LinkLocalAddress value
     */
    public String ipv6LinkLocalAddress() {
        return this.ipv6LinkLocalAddress;
    }

    /**
     * Get the list of DNS Servers of the device.
     *
     * @return the dnsServers value
     */
    public List<String> dnsServers() {
        return this.dnsServers;
    }

}
