/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import java.util.List;

/**
 * VpnClientConfiguration for P2S client.
 */
public class VpnClientConfiguration {
    /**
     * Gets or sets the reference of the Address space resource which
     * represents Address space for P2S VpnClient.
     */
    private AddressSpace vpnClientAddressPool;

    /**
     * VpnClientRootCertificate for Virtual network gateway.
     */
    private List<VpnClientRootCertificate> vpnClientRootCertificates;

    /**
     * VpnClientRevokedCertificate for Virtual network gateway.
     */
    private List<VpnClientRevokedCertificate> vpnClientRevokedCertificates;

    /**
     * Get the vpnClientAddressPool value.
     *
     * @return the vpnClientAddressPool value
     */
    public AddressSpace vpnClientAddressPool() {
        return this.vpnClientAddressPool;
    }

    /**
     * Set the vpnClientAddressPool value.
     *
     * @param vpnClientAddressPool the vpnClientAddressPool value to set
     * @return the VpnClientConfiguration object itself.
     */
    public VpnClientConfiguration withVpnClientAddressPool(AddressSpace vpnClientAddressPool) {
        this.vpnClientAddressPool = vpnClientAddressPool;
        return this;
    }

    /**
     * Get the vpnClientRootCertificates value.
     *
     * @return the vpnClientRootCertificates value
     */
    public List<VpnClientRootCertificate> vpnClientRootCertificates() {
        return this.vpnClientRootCertificates;
    }

    /**
     * Set the vpnClientRootCertificates value.
     *
     * @param vpnClientRootCertificates the vpnClientRootCertificates value to set
     * @return the VpnClientConfiguration object itself.
     */
    public VpnClientConfiguration withVpnClientRootCertificates(List<VpnClientRootCertificate> vpnClientRootCertificates) {
        this.vpnClientRootCertificates = vpnClientRootCertificates;
        return this;
    }

    /**
     * Get the vpnClientRevokedCertificates value.
     *
     * @return the vpnClientRevokedCertificates value
     */
    public List<VpnClientRevokedCertificate> vpnClientRevokedCertificates() {
        return this.vpnClientRevokedCertificates;
    }

    /**
     * Set the vpnClientRevokedCertificates value.
     *
     * @param vpnClientRevokedCertificates the vpnClientRevokedCertificates value to set
     * @return the VpnClientConfiguration object itself.
     */
    public VpnClientConfiguration withVpnClientRevokedCertificates(List<VpnClientRevokedCertificate> vpnClientRevokedCertificates) {
        this.vpnClientRevokedCertificates = vpnClientRevokedCertificates;
        return this;
    }

}
