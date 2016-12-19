/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.LoadBalancerFrontend;
import com.microsoft.azure.management.network.LoadBalancerInboundNatPool;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.TransportProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;

/**
 *  Implementation for {@link LoadBalancerInboundNatRule}.
 */
@LangDefinition
class LoadBalancerInboundNatPoolImpl
    extends ChildResourceImpl<InboundNatPoolInner, LoadBalancerImpl, LoadBalancer>
    implements
        LoadBalancerInboundNatPool,
        LoadBalancerInboundNatPool.Definition<LoadBalancer.DefinitionStages.WithCreateAndInboundNatPool>,
        LoadBalancerInboundNatPool.UpdateDefinition<LoadBalancer.Update>,
        LoadBalancerInboundNatPool.Update {

    LoadBalancerInboundNatPoolImpl(InboundNatPoolInner inner, LoadBalancerImpl parent) {
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
                        this.inner().frontendIPConfiguration().id()));
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
    public LoadBalancerInboundNatPoolImpl withBackendPort(int port) {
        this.inner().withBackendPort(port);
        return this;
    }

    @Override
    public LoadBalancerInboundNatPoolImpl withProtocol(TransportProtocol protocol) {
        this.inner().withProtocol(protocol);
        return this;
    }

    @Override
    public LoadBalancerInboundNatPoolImpl withFrontend(String frontendName) {
        SubResource frontendRef = new SubResource()
                .withId(this.parent().futureResourceId() + "/frontendIPConfigurations/" + frontendName);
        this.inner().withFrontendIPConfiguration(frontendRef);
        return this;
    }

    @Override
    public LoadBalancerInboundNatPoolImpl withFrontendPortRange(int from, int to) {
        this.inner().withFrontendPortRangeStart(from);
        this.inner().withFrontendPortRangeEnd(to);
        return this;
    }

    // Verbs

    @Override
    public LoadBalancerImpl attach() {
        return this.parent().withInboundNatPool(this);
    }
}
