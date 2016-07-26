/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.network.HttpProbe;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.ProbeProtocol;
import com.microsoft.azure.management.network.TcpProbe;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 *  Implementation for {@link TcpProbe} and its create and update interfaces.
 */
class ProbeImpl
    extends ChildResourceImpl<ProbeInner, LoadBalancerImpl>
    implements
        TcpProbe,
        TcpProbe.Definition<LoadBalancer.DefinitionStages.WithCreate>,
        TcpProbe.UpdateDefinition<LoadBalancer.Update>,
        TcpProbe.Update,
        HttpProbe,
        HttpProbe.Definition<LoadBalancer.DefinitionStages.WithCreate>,
        HttpProbe.UpdateDefinition<LoadBalancer.Update>,
        HttpProbe.Update {

    protected ProbeImpl(String name, ProbeInner inner, LoadBalancerImpl parent) {
        super(name, inner, parent);
    }

    // Getters

    @Override
    public int intervalInSeconds() {
        return this.inner().intervalInSeconds();
    }

    @Override
    public int port() {
        return this.inner().port();
    }

    @Override
    public int numberOfProbes() {
        return this.inner().numberOfProbes();
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public ProbeProtocol protocol() {
        return this.inner().protocol();
    }

    @Override
    public String requestPath() {
        return this.inner().requestPath();
    }

    // Fluent setters

    @Override
    public ProbeImpl withPort(int port) {
        this.inner().withPort(port);
        return this;
    }

    @Override
    public ProbeImpl withRequestPath(String requestPath) {
        this.inner().withRequestPath(requestPath);
        return this;
    }

    @Override
    public ProbeImpl withIntervalInSeconds(int seconds) {
        this.inner().withIntervalInSeconds(seconds);
        return this;
    }

    @Override
    public ProbeImpl withNumberOfProbes(int probes) {
        this.inner().withNumberOfProbes(probes);
        return this;
    }

    // Verbs

    @Override
    public LoadBalancerImpl attach() {
        return this.parent().withProbe(this);
    }
}
