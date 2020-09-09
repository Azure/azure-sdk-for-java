// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.NextHop;
import com.azure.resourcemanager.network.models.NextHopParameters;
import com.azure.resourcemanager.network.models.NextHopType;
import com.azure.resourcemanager.network.fluent.inner.NextHopResultInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.ExecutableImpl;
import reactor.core.publisher.Mono;

/** Implementation of NextHop. */
public class NextHopImpl extends ExecutableImpl<NextHop> implements NextHop, NextHop.Definition {
    private final NetworkWatcherImpl parent;
    private NextHopParameters parameters = new NextHopParameters();
    private NextHopResultInner result;

    NextHopImpl(NetworkWatcherImpl parent) {
        this.parent = parent;
    }

    @Override
    public NextHopImpl withTargetResourceId(String targetResourceId) {
        this.parameters.withTargetResourceId(targetResourceId);
        return this;
    }

    @Override
    public NextHopImpl withSourceIpAddress(String sourceIpAddress) {
        this.parameters.withSourceIpAddress(sourceIpAddress);
        return this;
    }

    @Override
    public NextHopImpl withDestinationIpAddress(String destinationIpAddress) {
        this.parameters.withDestinationIpAddress(destinationIpAddress);
        return this;
    }

    @Override
    public NextHopImpl withTargetNetworkInterfaceId(String targetNetworkInterfaceId) {
        this.parameters.withTargetNicResourceId(targetNetworkInterfaceId);
        return this;
    }

    @Override
    public NetworkWatcherImpl parent() {
        return parent;
    }

    @Override
    public String targetResourceId() {
        return parameters.targetResourceId();
    }

    @Override
    public String sourceIpAddress() {
        return parameters.sourceIpAddress();
    }

    @Override
    public String destinationIpAddress() {
        return parameters.destinationIpAddress();
    }

    @Override
    public String targetNetworkInterfaceId() {
        return parameters.destinationIpAddress();
    }

    @Override
    public NextHopType nextHopType() {
        return result.nextHopType();
    }

    @Override
    public String nextHopIpAddress() {
        return result.nextHopIpAddress();
    }

    @Override
    public String routeTableId() {
        return result.routeTableId();
    }

    @Override
    public Mono<NextHop> executeWorkAsync() {
        return this
            .parent()
            .manager()
            .inner()
            .getNetworkWatchers()
            .getNextHopAsync(parent.resourceGroupName(), parent.name(), parameters)
            .map(
                nextHopResultInner -> {
                    NextHopImpl.this.result = nextHopResultInner;
                    return NextHopImpl.this;
                });
    }
}
