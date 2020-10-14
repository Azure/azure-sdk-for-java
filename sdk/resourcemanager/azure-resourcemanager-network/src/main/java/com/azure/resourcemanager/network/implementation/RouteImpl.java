// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.Route;
import com.azure.resourcemanager.network.models.RouteNextHopType;
import com.azure.resourcemanager.network.models.RouteTable;
import com.azure.resourcemanager.network.fluent.models.RouteInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/** Implementation of Route. */
class RouteImpl extends ChildResourceImpl<RouteInner, RouteTableImpl, RouteTable>
    implements Route,
        Route.Definition<RouteTable.DefinitionStages.WithCreate>,
        Route.UpdateDefinition<RouteTable.Update>,
        Route.Update {

    RouteImpl(RouteInner inner, RouteTableImpl parent) {
        super(inner, parent);
    }

    // Getters
    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public String destinationAddressPrefix() {
        return this.innerModel().addressPrefix();
    }

    @Override
    public RouteNextHopType nextHopType() {
        return this.innerModel().nextHopType();
    }

    @Override
    public String nextHopIpAddress() {
        return this.innerModel().nextHopIpAddress();
    }

    // Fluent setters

    @Override
    public RouteImpl withNextHop(RouteNextHopType nextHopType) {
        this.innerModel().withNextHopType(nextHopType);
        return this;
    }

    @Override
    public RouteImpl withDestinationAddressPrefix(String cidr) {
        this.innerModel().withAddressPrefix(cidr);
        return this;
    }

    @Override
    public RouteImpl withNextHopToVirtualAppliance(String ipAddress) {
        this.innerModel().withNextHopType(RouteNextHopType.VIRTUAL_APPLIANCE).withNextHopIpAddress(ipAddress);
        return this;
    }

    // Verbs

    @Override
    public RouteTableImpl attach() {
        return this.parent().withRoute(this);
    }
}
