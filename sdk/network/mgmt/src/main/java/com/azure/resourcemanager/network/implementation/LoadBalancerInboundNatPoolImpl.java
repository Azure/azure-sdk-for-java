// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.models.InboundNatPool;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.LoadBalancerFrontend;
import com.azure.resourcemanager.network.models.LoadBalancerInboundNatPool;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.Subnet;
import com.azure.resourcemanager.network.models.TransportProtocol;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;

/** Implementation for LoadBalancerInboundNatRule. */
class LoadBalancerInboundNatPoolImpl extends ChildResourceImpl<InboundNatPool, LoadBalancerImpl, LoadBalancer>
    implements LoadBalancerInboundNatPool,
        LoadBalancerInboundNatPool.Definition<LoadBalancer.DefinitionStages.WithCreateAndInboundNatPool>,
        LoadBalancerInboundNatPool.UpdateDefinition<LoadBalancer.Update>,
        LoadBalancerInboundNatPool.Update {

    LoadBalancerInboundNatPoolImpl(InboundNatPool inner, LoadBalancerImpl parent) {
        super(inner, parent);
    }

    // Getters

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public TransportProtocol protocol() {
        return this.inner().protocol();
    }

    @Override
    public int backendPort() {
        return Utils.toPrimitiveInt(this.inner().backendPort());
    }

    @Override
    public LoadBalancerFrontend frontend() {
        return this
            .parent()
            .frontends()
            .get(ResourceUtils.nameFromResourceId(this.inner().frontendIpConfiguration().id()));
    }

    @Override
    public int frontendPortRangeStart() {
        return Utils.toPrimitiveInt(this.inner().frontendPortRangeStart());
    }

    @Override
    public int frontendPortRangeEnd() {
        return Utils.toPrimitiveInt(this.inner().frontendPortRangeEnd());
    }

    // Fluent setters

    @Override
    public LoadBalancerInboundNatPoolImpl toBackendPort(int port) {
        this.inner().withBackendPort(port);
        return this;
    }

    @Override
    public LoadBalancerInboundNatPoolImpl withProtocol(TransportProtocol protocol) {
        this.inner().withProtocol(protocol);
        return this;
    }

    @Override
    public LoadBalancerInboundNatPoolImpl fromFrontend(String frontendName) {
        SubResource frontendRef = this.parent().ensureFrontendRef(frontendName);
        if (frontendRef != null) {
            this.inner().withFrontendIpConfiguration(frontendRef);
        }
        return this;
    }

    @Override
    public LoadBalancerInboundNatPoolImpl fromFrontendPortRange(int from, int to) {
        this.inner().withFrontendPortRangeStart(from).withFrontendPortRangeEnd(to);
        return this;
    }

    // Verbs

    @Override
    public LoadBalancerImpl attach() {
        return this.parent().withInboundNatPool(this);
    }

    @Override
    public LoadBalancerInboundNatPoolImpl fromExistingPublicIPAddress(PublicIpAddress publicIPAddress) {
        return (publicIPAddress != null) ? this.fromExistingPublicIPAddress(publicIPAddress.id()) : this;
    }

    @Override
    public LoadBalancerInboundNatPoolImpl fromExistingPublicIPAddress(String resourceId) {
        return (null != resourceId)
            ? this.fromFrontend(this.parent().ensurePublicFrontendWithPip(resourceId).name())
            : this;
    }

    @Override
    public LoadBalancerInboundNatPoolImpl fromNewPublicIPAddress(String leafDnsLabel) {
        String frontendName = this.parent().manager().sdkContext().randomResourceName("fe", 20);
        this.parent().withNewPublicIPAddress(leafDnsLabel, frontendName);
        return fromFrontend(frontendName);
    }

    @Override
    public LoadBalancerInboundNatPoolImpl fromNewPublicIPAddress(Creatable<PublicIpAddress> pipDefinition) {
        String frontendName = this.parent().manager().sdkContext().randomResourceName("fe", 20);
        this.parent().withNewPublicIPAddress(pipDefinition, frontendName);
        return fromFrontend(frontendName);
    }

    @Override
    public LoadBalancerInboundNatPoolImpl fromNewPublicIPAddress() {
        String dnsLabel = this.parent().manager().sdkContext().randomResourceName("fe", 20);
        return this.fromNewPublicIPAddress(dnsLabel);
    }

    @Override
    public LoadBalancerInboundNatPoolImpl fromExistingSubnet(String networkResourceId, String subnetName) {
        return (null != networkResourceId && null != subnetName)
            ? this.fromFrontend(this.parent().ensurePrivateFrontendWithSubnet(networkResourceId, subnetName).name())
            : this;
    }

    @Override
    public LoadBalancerInboundNatPoolImpl fromExistingSubnet(Network network, String subnetName) {
        return (null != network && null != subnetName) ? this.fromExistingSubnet(network.id(), subnetName) : this;
    }

    @Override
    public LoadBalancerInboundNatPoolImpl fromExistingSubnet(Subnet subnet) {
        return (null != subnet) ? this.fromExistingSubnet(subnet.parent().id(), subnet.name()) : this;
    }
}
