// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.ExpressRouteCircuitReference;
import com.azure.resourcemanager.network.models.ExpressRouteCrossConnection;
import com.azure.resourcemanager.network.models.ExpressRouteCrossConnectionPeering;
import com.azure.resourcemanager.network.models.ExpressRouteCrossConnectionPeerings;
import com.azure.resourcemanager.network.models.ServiceProviderProvisioningState;
import com.azure.resourcemanager.network.fluent.inner.ExpressRouteCrossConnectionInner;
import com.azure.resourcemanager.network.fluent.inner.ExpressRouteCrossConnectionPeeringInner;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import reactor.core.publisher.Mono;

/** Implementation for ExpressRouteCrossConnection. */
public class ExpressRouteCrossConnectionImpl
    extends GroupableParentResourceWithTagsImpl<
        ExpressRouteCrossConnection, ExpressRouteCrossConnectionInner, ExpressRouteCrossConnectionImpl, NetworkManager>
    implements ExpressRouteCrossConnection, ExpressRouteCrossConnection.Update {
    private ExpressRouteCrossConnectionPeeringsImpl peerings;
    private Map<String, ExpressRouteCrossConnectionPeering> crossConnectionPeerings;

    ExpressRouteCrossConnectionImpl(String name, ExpressRouteCrossConnectionInner innerObject, NetworkManager manager) {
        super(name, innerObject, manager);
        initializeChildrenFromInner();
    }

    @Override
    protected Mono<ExpressRouteCrossConnectionInner> createInner() {
        return this
            .manager()
            .inner()
            .getExpressRouteCrossConnections()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner());
    }

    @Override
    protected void initializeChildrenFromInner() {
        crossConnectionPeerings = new HashMap<>();
        if (inner().peerings() != null) {
            for (ExpressRouteCrossConnectionPeeringInner peering : inner().peerings()) {
                crossConnectionPeerings
                    .put(
                        peering.name(),
                        new ExpressRouteCrossConnectionPeeringImpl(this, peering, peering.peeringType()));
            }
        }
    }

    @Override
    protected Mono<ExpressRouteCrossConnectionInner> getInnerAsync() {
        return this
            .manager()
            .inner()
            .getExpressRouteCrossConnections()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public Mono<ExpressRouteCrossConnection> refreshAsync() {
        return super
            .refreshAsync()
            .map(
                expressRouteCrossConnection -> {
                    ExpressRouteCrossConnectionImpl impl =
                        (ExpressRouteCrossConnectionImpl) expressRouteCrossConnection;
                    impl.initializeChildrenFromInner();
                    return impl;
                });
    }

    @Override
    protected Mono<ExpressRouteCrossConnectionInner> applyTagsToInnerAsync() {
        return this
            .manager()
            .inner()
            .getExpressRouteCrossConnections()
            .updateTagsAsync(resourceGroupName(), name(), inner().tags());
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
    public Integer stag() {
        return inner().stag();
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
        return inner().provisioningState().toString();
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
