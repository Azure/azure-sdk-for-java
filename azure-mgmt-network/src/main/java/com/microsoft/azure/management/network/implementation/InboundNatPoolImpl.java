/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.Frontend;
import com.microsoft.azure.management.network.InboundNatPool;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.TransportProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 *  Implementation for {@link InboundNatRule}.
 */
@LangDefinition
class InboundNatPoolImpl
    extends ChildResourceImpl<InboundNatPoolInner, LoadBalancerImpl, LoadBalancer>
    implements
        InboundNatPool,
        InboundNatPool.Definition<LoadBalancer.DefinitionStages.WithCreateAndInboundNatPool>,
        InboundNatPool.UpdateDefinition<LoadBalancer.Update>,
        InboundNatPool.Update {

    InboundNatPoolImpl(InboundNatPoolInner inner, LoadBalancerImpl parent) {
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
        return this.inner().backendPort();
    }

    @Override
    public Frontend frontend() {
        return this.parent().frontends().get(
                ResourceUtils.nameFromResourceId(
                        this.inner().frontendIPConfiguration().id()));
    }

    @Override
    public int frontendPortRangeStart() {
        return this.inner().frontendPortRangeStart();
    }

    @Override
    public int frontendPortRangeEnd() {
        return this.inner().frontendPortRangeEnd();
    }

    // Fluent setters

    @Override
    public InboundNatPoolImpl withBackendPort(int port) {
        this.inner().withBackendPort(port);
        return this;
    }

    @Override
    public InboundNatPoolImpl withProtocol(TransportProtocol protocol) {
        this.inner().withProtocol(protocol);
        return this;
    }

    @Override
    public InboundNatPoolImpl withFrontend(String frontendName) {
        SubResource frontendRef = new SubResource()
                .withId(this.parent().futureResourceId() + "/frontendIPConfigurations/" + frontendName);
        this.inner().withFrontendIPConfiguration(frontendRef);
        return this;
    }

    @Override
    public InboundNatPoolImpl withFrontendPortRange(int from, int to) {
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
