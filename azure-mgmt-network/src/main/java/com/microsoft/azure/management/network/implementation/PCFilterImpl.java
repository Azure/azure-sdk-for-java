/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.PCFilter;
import com.microsoft.azure.management.network.PacketCapture;
import com.microsoft.azure.management.network.PacketCaptureFilter;
import com.microsoft.azure.management.network.PcProtocol;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;

/**
 * Represents Packet Capture filter.
 */
@LangDefinition
class PCFilterImpl extends IndexableWrapperImpl<PacketCaptureFilter>
        implements
        PCFilter,
        PCFilter.Definition<PacketCapture.DefinitionStages.WithCreate> {
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
    public PCFilterImpl withRemoteIPAddress(String ipAddress) {
        this.inner().withRemoteIPAddress(ipAddress);
        return this;
    }

    @Override
    public PCFilterImpl withLocalPort(String localPort) {
        this.inner().withLocalPort(localPort);
        return this;
    }

    @Override
    public PCFilterImpl withRemotePort(String remotePort) {
        this.inner().withRemotePort(remotePort);
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
}
