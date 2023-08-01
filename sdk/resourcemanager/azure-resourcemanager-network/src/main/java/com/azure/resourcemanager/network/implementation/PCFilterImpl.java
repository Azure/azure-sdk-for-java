// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.PCFilter;
import com.azure.resourcemanager.network.models.PacketCapture;
import com.azure.resourcemanager.network.models.PacketCaptureFilter;
import com.azure.resourcemanager.network.models.PcProtocol;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import java.util.List;

/** Represents Packet Capture filter. */
class PCFilterImpl extends IndexableWrapperImpl<PacketCaptureFilter>
    implements PCFilter, PCFilter.Definition<PacketCapture.DefinitionStages.WithCreate> {
    private static final String DELIMITER = ";";
    private static final String RANGE_DELIMITER = "-";
    private PacketCaptureImpl parent;

    PCFilterImpl(PacketCaptureFilter inner, PacketCaptureImpl parent) {
        super(inner);
        this.parent = parent;
    }

    @Override
    public PcProtocol protocol() {
        return this.innerModel().protocol();
    }

    @Override
    public String localIpAddress() {
        return this.innerModel().localIpAddress();
    }

    @Override
    public String remoteIpAddress() {
        return this.innerModel().remoteIpAddress();
    }

    @Override
    public String localPort() {
        return this.innerModel().localPort();
    }

    @Override
    public String remotePort() {
        return this.innerModel().remotePort();
    }

    @Override
    public PCFilterImpl withProtocol(PcProtocol protocol) {
        this.innerModel().withProtocol(protocol);
        return this;
    }

    @Override
    public PCFilterImpl withLocalIpAddress(String ipAddress) {
        this.innerModel().withLocalIpAddress(ipAddress);
        return this;
    }

    @Override
    public Definition<PacketCapture.DefinitionStages.WithCreate> withLocalIpAddressesRange(
        String startIpAddress, String endIpAddress) {
        this.innerModel().withLocalIpAddress(startIpAddress + RANGE_DELIMITER + endIpAddress);
        return this;
    }

    @Override
    public Definition<PacketCapture.DefinitionStages.WithCreate> withLocalIpAddresses(List<String> ipAddresses) {
        StringBuilder ipAddressesString = new StringBuilder();
        for (String ipAddress : ipAddresses) {
            ipAddressesString.append(ipAddress).append(DELIMITER);
        }
        this.innerModel().withLocalIpAddress(ipAddressesString.substring(0, ipAddressesString.length() - 1));
        return this;
    }

    @Override
    public PCFilterImpl withRemoteIpAddress(String ipAddress) {
        this.innerModel().withRemoteIpAddress(ipAddress);
        return this;
    }

    @Override
    public Definition<PacketCapture.DefinitionStages.WithCreate> withRemoteIpAddressesRange(
        String startIpAddress, String endIpAddress) {
        this.innerModel().withRemoteIpAddress(startIpAddress + RANGE_DELIMITER + endIpAddress);
        return this;
    }

    @Override
    public Definition<PacketCapture.DefinitionStages.WithCreate> withRemoteIpAddresses(List<String> ipAddresses) {
        StringBuilder ipAddressesString = new StringBuilder();
        for (String ipAddress : ipAddresses) {
            ipAddressesString.append(ipAddress).append(DELIMITER);
        }
        this.innerModel().withRemoteIpAddress(ipAddressesString.substring(0, ipAddressesString.length() - 1));
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
        this.innerModel().withLocalPort(String.valueOf(port));
        return this;
    }

    @Override
    public Definition<PacketCapture.DefinitionStages.WithCreate> withLocalPortRange(int startPort, int endPort) {
        this.innerModel().withLocalPort(startPort + RANGE_DELIMITER + endPort);
        return this;
    }

    @Override
    public Definition<PacketCapture.DefinitionStages.WithCreate> withLocalPorts(List<Integer> ports) {
        StringBuilder portsString = new StringBuilder();
        for (int port : ports) {
            portsString.append(port).append(DELIMITER);
        }
        this.innerModel().withLocalPort(portsString.substring(0, portsString.length() - 1));
        return this;
    }

    @Override
    public Definition<PacketCapture.DefinitionStages.WithCreate> withRemotePort(int port) {
        this.innerModel().withRemotePort(String.valueOf(port));
        return this;
    }

    @Override
    public Definition<PacketCapture.DefinitionStages.WithCreate> withRemotePortRange(int startPort, int endPort) {
        this.innerModel().withRemotePort(startPort + RANGE_DELIMITER + endPort);
        return this;
    }

    @Override
    public Definition<PacketCapture.DefinitionStages.WithCreate> withRemotePorts(List<Integer> ports) {
        StringBuilder portsString = new StringBuilder();
        for (int port : ports) {
            portsString.append(port).append(DELIMITER);
        }
        this.innerModel().withRemotePort(portsString.substring(0, portsString.length() - 1));
        return this;
    }
}
