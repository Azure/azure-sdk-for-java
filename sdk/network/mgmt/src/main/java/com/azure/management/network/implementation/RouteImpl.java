/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.management.network.Route;
import com.azure.management.network.RouteNextHopType;
import com.azure.management.network.RouteTable;
import com.azure.management.network.models.RouteInner;
import com.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 * Implementation of Route.
 */
class RouteImpl
        extends ChildResourceImpl<RouteInner, RouteTableImpl, RouteTable>
        implements
        Route,
        Route.Definition<RouteTable.DefinitionStages.WithCreate>,
        Route.UpdateDefinition<RouteTable.Update>,
        Route.Update {

    RouteImpl(RouteInner inner, RouteTableImpl parent) {
        super(inner, parent);
    }

    // Getters
    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String destinationAddressPrefix() {
        return this.inner().addressPrefix();
    }

    @Override
    public RouteNextHopType nextHopType() {
        return this.inner().nextHopType();
    }

    @Override
    public String nextHopIPAddress() {
        return this.inner().nextHopIpAddress();
    }

    // Fluent setters

    @Override
    public RouteImpl withNextHop(RouteNextHopType nextHopType) {
        this.inner().withNextHopType(nextHopType);
        return this;
    }

    @Override
    public RouteImpl withDestinationAddressPrefix(String cidr) {
        this.inner().withAddressPrefix(cidr);
        return this;
    }

    @Override
    public RouteImpl withNextHopToVirtualAppliance(String ipAddress) {
        this.inner()
                .withNextHopType(RouteNextHopType.VIRTUAL_APPLIANCE)
                .withNextHopIpAddress(ipAddress);
        return this;
    }

    // Verbs

    @Override
    public RouteTableImpl attach() {
        return this.parent().withRoute(this);
    }
}
