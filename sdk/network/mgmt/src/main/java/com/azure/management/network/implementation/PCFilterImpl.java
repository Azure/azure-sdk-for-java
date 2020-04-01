/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.management.network.PCFilter;
import com.azure.management.network.PacketCapture;
import com.azure.management.network.PacketCaptureFilter;
import com.azure.management.network.PcProtocol;
import com.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;

import java.util.List;

/**
 * Represents Packet Capture filter.
 */
class PCFilterImpl extends IndexableWrapperImpl<PacketCaptureFilter>
        implements
        PCFilter,
        PCFilter.Definition<PacketCapture.DefinitionStages.WithCreate> {
    private static final String DELIMITER = ";";
    private static final String RANGE_DELIMITER = "-";
    private PacketCaptureImpl parent;

    PCFilterImpl(PacketCaptureFilter inner, PacketCaptureImpl parent) {
        super(inner);
        this.parent = parent;
    }

    @Override
    public PcProtocol protocol() {
        return this.inner().protocol();
    }

    @Override
    public String localIPAddress() {
        return this.inner().localIPAddress();
    }

    @Override
    public String remoteIPAddress() {
        return this.inner().remoteIPAddress();
    }

    @Override
    public String localPort() {
        return this.inner().localPort();
    }

    @Override
    public String remotePort() {
        return this.inner().remotePort();
    }

    @Override
    public PCFilterImpl withProtocol(PcProtocol protocol) {
        this.inner().withProtocol(protocol);
        return this;
    }

    @Override
    public PCFilterImpl withLocalIPAddress(String ipAddress) {
        this.inner().withLocalIPAddress(ipAddress);
        return this;
    }

    @Override
    public Definition<PacketCapture.DefinitionStages.WithCreate> withLocalIPAddressesRange(String startIPAddress, String endIPAddress) {
        this.inner().withLocalIPAddress(startIPAddress + RANGE_DELIMITER + endIPAddress);
        return this;
    }

    @Override
    public Definition<PacketCapture.DefinitionStages.WithCreate> withLocalIPAddresses(List<String> ipAddresses) {
        StringBuilder ipAddressesString = new StringBuilder();
        for (String ipAddress : ipAddresses) {
            ipAddressesString.append(ipAddress).append(DELIMITER);
        }
        this.inner().withLocalIPAddress(ipAddressesString.substring(0, ipAddressesString.length() - 1));
        return this;
    }

    @Override
    public PCFilterImpl withRemoteIPAddress(String ipAddress) {
        this.inner().withRemoteIPAddress(ipAddress);
        return this;
    }

    @Override
    public Definition<PacketCapture.DefinitionStages.WithCreate> withRemoteIPAddressesRange(String startIPAddress, String endIPAddress) {
        this.inner().withRemoteIPAddress(startIPAddress + RANGE_DELIMITER + endIPAddress);
        return this;
    }

    @Override
    public Definition<PacketCapture.DefinitionStages.WithCreate> withRemoteIPAddresses(List<String> ipAddresses) {
        StringBuilder ipAddressesString = new StringBuilder();
        for (String ipAddress : ipAddresses) {
            ipAddressesString.append(ipAddress).append(DELIMITER);
        }
        this.inner().withRemoteIPAddress(ipAddressesString.substring(0, ipAddressesString.length() - 1));
        return this;
    }

    @Override
    public PacketCapture parent() {
        return parent;
    }

    @Override
    public PacketCaptureImpl attach() {
        this.parent.attachPCFilter(this);
        return parent;
    }

    @Override
    public Definition<PacketCapture.DefinitionStages.WithCreate> withLocalPort(int port) {
        this.inner().withLocalPort(String.valueOf(port));
        return this;
    }

    @Override
    public Definition<PacketCapture.DefinitionStages.WithCreate> withLocalPortRange(int startPort, int endPort) {
        this.inner().withLocalPort(startPort + RANGE_DELIMITER + endPort);
        return this;
    }

    @Override
    public Definition<PacketCapture.DefinitionStages.WithCreate> withLocalPorts(List<Integer> ports) {
        StringBuilder portsString = new StringBuilder();
        for (int port : ports) {
            portsString.append(port).append(DELIMITER);
        }
        this.inner().withLocalPort(portsString.substring(0, portsString.length() - 1));
        return this;
    }

    @Override
    public Definition<PacketCapture.DefinitionStages.WithCreate> withRemotePort(int port) {
        this.inner().withRemotePort(String.valueOf(port));
        return this;
    }

    @Override
    public Definition<PacketCapture.DefinitionStages.WithCreate> withRemotePortRange(int startPort, int endPort) {
        this.inner().withRemotePort(startPort + RANGE_DELIMITER + endPort);
        return this;
    }

    @Override
    public Definition<PacketCapture.DefinitionStages.WithCreate> withRemotePorts(List<Integer> ports) {
        StringBuilder portsString = new StringBuilder();
        for (int port : ports) {
            portsString.append(port).append(DELIMITER);
        }
        this.inner().withRemotePort(portsString.substring(0, portsString.length() - 1));
        return this;
    }
}
