/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.management.network.ExpressRouteCircuitReference;
import com.azure.management.network.ExpressRouteCrossConnection;
import com.azure.management.network.ExpressRouteCrossConnectionPeering;
import com.azure.management.network.ExpressRouteCrossConnectionPeerings;
import com.azure.management.network.ServiceProviderProvisioningState;
import com.azure.management.network.models.ExpressRouteCrossConnectionInner;
import com.azure.management.network.models.ExpressRouteCrossConnectionPeeringInner;
import com.azure.management.network.models.GroupableParentResourceWithTagsImpl;
import com.azure.management.resources.fluentcore.utils.Utils;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation for ExpressRouteCrossConnection.
 */
public class ExpressRouteCrossConnectionImpl extends GroupableParentResourceWithTagsImpl<
        ExpressRouteCrossConnection,
        ExpressRouteCrossConnectionInner,
        ExpressRouteCrossConnectionImpl,
        NetworkManager>
        implements
        ExpressRouteCrossConnection,
        ExpressRouteCrossConnection.Update {
    private ExpressRouteCrossConnectionPeeringsImpl peerings;
    private Map<String, ExpressRouteCrossConnectionPeering> crossConnectionPeerings;

    ExpressRouteCrossConnectionImpl(String name, ExpressRouteCrossConnectionInner innerObject, NetworkManager manager) {
        super(name, innerObject, manager);
        initializeChildrenFromInner();
    }

    @Override
    protected Mono<ExpressRouteCrossConnectionInner> createInner() {
        return this.manager().inner().expressRouteCrossConnections().createOrUpdateAsync(
                this.resourceGroupName(), this.name(), this.inner());
    }

    @Override
    protected void initializeChildrenFromInner() {
        crossConnectionPeerings = new HashMap<>();
        if (inner().peerings() != null) {
            for (ExpressRouteCrossConnectionPeeringInner peering : inner().peerings()) {
                crossConnectionPeerings.put(peering.name(),
                        new ExpressRouteCrossConnectionPeeringImpl(this, peering, peering.peeringType()));
            }
        }
    }

    @Override
    protected Mono<ExpressRouteCrossConnectionInner> getInnerAsync() {
        return this.manager().inner().expressRouteCrossConnections().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public Mono<ExpressRouteCrossConnection> refreshAsync() {
        return super.refreshAsync().map(expressRouteCrossConnection -> {
            ExpressRouteCrossConnectionImpl impl = (ExpressRouteCrossConnectionImpl) expressRouteCrossConnection;
            impl.initializeChildrenFromInner();
            return impl;
        });
    }

    @Override
    protected Mono<ExpressRouteCrossConnectionInner> applyTagsToInnerAsync() {
        return this.manager().inner().expressRouteCrossConnections().updateTagsAsync(resourceGroupName(), name(), inner().getTags());
    }

    @Override
    public ExpressRouteCrossConnectionPeerings peerings() {
        if (peerings == null) {
            peerings = new ExpressRouteCrossConnectionPeeringsImpl(this);
        }
        return peerings;
    }

    @Override
    public String primaryAzurePort() {
        return inner().primaryAzurePort();
    }

    @Override
    public String secondaryAzurePort() {
        return inner().secondaryAzurePort();
    }

    @Override
    public Integer sTag() {
        return inner().sTag();
    }

    @Override
    public String peeringLocation() {
        return inner().peeringLocation();
    }

    @Override
    public int bandwidthInMbps() {
        return Utils.toPrimitiveInt(inner().bandwidthInMbps());
    }

    @Override
    public ExpressRouteCircuitReference expressRouteCircuit() {
        return inner().expressRouteCircuit();
    }

    @Override
    public ServiceProviderProvisioningState serviceProviderProvisioningState() {
        return inner().serviceProviderProvisioningState();
    }

    @Override
    public String serviceProviderNotes() {
        return inner().serviceProviderNotes();
    }

    @Override
    public String provisioningState() {
        return inner().provisioningState();
    }

    @Override
    public Map<String, ExpressRouteCrossConnectionPeering> peeringsMap() {
        return Collections.unmodifiableMap(crossConnectionPeerings);
    }

    @Override
    public Update withServiceProviderProvisioningState(ServiceProviderProvisioningState state) {
        inner().withServiceProviderProvisioningState(state);
        return this;
    }

    @Override
    public Update withServiceProviderNotes(String notes) {
        inner().withServiceProviderNotes(notes);
        return this;
    }
}
