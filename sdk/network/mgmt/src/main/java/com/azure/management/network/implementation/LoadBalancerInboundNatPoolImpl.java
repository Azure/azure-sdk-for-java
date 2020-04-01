/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.management.network.InboundNatPool;
import com.azure.management.network.LoadBalancer;
import com.azure.management.network.LoadBalancerFrontend;
import com.azure.management.network.LoadBalancerInboundNatPool;
import com.azure.management.network.Network;
import com.azure.management.network.PublicIPAddress;
import com.azure.management.network.Subnet;
import com.azure.management.network.TransportProtocol;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.azure.management.resources.fluentcore.model.Creatable;
import com.azure.management.resources.fluentcore.utils.Utils;

/**
 * Implementation for LoadBalancerInboundNatRule.
 */
class LoadBalancerInboundNatPoolImpl
        extends ChildResourceImpl<InboundNatPool, LoadBalancerImpl, LoadBalancer>
        implements
        LoadBalancerInboundNatPool,
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
        return this.parent().frontends().get(
                ResourceUtils.nameFromResourceId(
                        this.inner().frontendIPConfiguration().getId()));
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
            this.inner().withFrontendIPConfiguration(frontendRef);
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
    public LoadBalancerInboundNatPoolImpl fromExistingPublicIPAddress(PublicIPAddress publicIPAddress) {
        return (publicIPAddress != null) ? this.fromExistingPublicIPAddress(publicIPAddress.id()) : this;
    }

    @Override
    public LoadBalancerInboundNatPoolImpl fromExistingPublicIPAddress(String resourceId) {
        return (null != resourceId) ? this.fromFrontend(this.parent().ensurePublicFrontendWithPip(resourceId).name()) : this;
    }

    @Override
    public LoadBalancerInboundNatPoolImpl fromNewPublicIPAddress(String leafDnsLabel) {
        String frontendName = this.parent().manager().getSdkContext().randomResourceName("fe", 20);
        this.parent().withNewPublicIPAddress(leafDnsLabel, frontendName);
        return fromFrontend(frontendName);
    }

    @Override
    public LoadBalancerInboundNatPoolImpl fromNewPublicIPAddress(Creatable<PublicIPAddress> pipDefinition) {
        String frontendName = this.parent().manager().getSdkContext().randomResourceName("fe", 20);
        this.parent().withNewPublicIPAddress(pipDefinition, frontendName);
        return fromFrontend(frontendName);
    }

    @Override
    public LoadBalancerInboundNatPoolImpl fromNewPublicIPAddress() {
        String dnsLabel = this.parent().manager().getSdkContext().randomResourceName("fe", 20);
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
        return (null != network && null != subnetName)
                ? this.fromExistingSubnet(network.id(), subnetName)
                : this;
    }

    @Override
    public LoadBalancerInboundNatPoolImpl fromExistingSubnet(Subnet subnet) {
        return (null != subnet)
                ? this.fromExistingSubnet(subnet.parent().id(), subnet.name())
                : this;
    }
}
