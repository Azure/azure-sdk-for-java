/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.NextHop;
import com.microsoft.azure.management.network.NextHopType;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.ExecutableImpl;
import rx.Observable;
import rx.functions.Func1;

/**
 * Implementation of NextHop.
 */
@LangDefinition
public class NextHopImpl extends ExecutableImpl<NextHop>
        implements NextHop, NextHop.Definition {
    private final NetworkWatcherImpl parent;
    private NextHopParametersInner parameters = new NextHopParametersInner();
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
    public Observable<NextHop> executeWorkAsync() {
        return this.parent().manager().inner().networkWatchers()
                .getNextHopAsync(parent.resourceGroupName(), parent.name(), parameters)
                .map(new Func1<NextHopResultInner, NextHop>() {
                    @Override
                    public NextHop call(NextHopResultInner nextHopResultInner) {
                        NextHopImpl.this.result = nextHopResultInner;
                        return NextHopImpl.this;
                    }
                });
    }
}
