/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.management.network.NextHop;
import com.azure.management.network.NextHopParameters;
import com.azure.management.network.NextHopType;
import com.azure.management.network.models.NextHopResultInner;
import com.azure.management.resources.fluentcore.model.implementation.ExecutableImpl;
import reactor.core.publisher.Mono;

/**
 * Implementation of NextHop.
 */
public class NextHopImpl extends ExecutableImpl<NextHop>
        implements NextHop, NextHop.Definition {
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
    public NextHopImpl withSourceIPAddress(String sourceIPAddress) {
        this.parameters.withSourceIPAddress(sourceIPAddress);
        return this;
    }

    @Override
    public NextHopImpl withDestinationIPAddress(String destinationIPAddress) {
        this.parameters.withDestinationIPAddress(destinationIPAddress);
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
    public String sourceIPAddress() {
        return parameters.sourceIPAddress();
    }

    @Override
    public String destinationIPAddress() {
        return parameters.destinationIPAddress();
    }

    @Override
    public String targetNetworkInterfaceId() {
        return parameters.destinationIPAddress();
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
        return this.parent().manager().inner().networkWatchers()
                .getNextHopAsync(parent.resourceGroupName(), parent.name(), parameters)
                .map(nextHopResultInner -> {
                    NextHopImpl.this.result = nextHopResultInner;
                    return NextHopImpl.this;
                });
    }
}
