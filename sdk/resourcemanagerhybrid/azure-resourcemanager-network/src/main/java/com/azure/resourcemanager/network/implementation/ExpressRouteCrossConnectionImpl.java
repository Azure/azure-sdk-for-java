// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.ExpressRouteCircuitReference;
import com.azure.resourcemanager.network.models.ExpressRouteCrossConnection;
import com.azure.resourcemanager.network.models.ExpressRouteCrossConnectionPeering;
import com.azure.resourcemanager.network.models.ExpressRouteCrossConnectionPeerings;
import com.azure.resourcemanager.network.models.ServiceProviderProvisioningState;
import com.azure.resourcemanager.network.fluent.models.ExpressRouteCrossConnectionInner;
import com.azure.resourcemanager.network.fluent.models.ExpressRouteCrossConnectionPeeringInner;
import com.azure.resourcemanager.network.models.TagsObject;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
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
            .serviceClient()
            .getExpressRouteCrossConnections()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.innerModel());
    }

    @Override
    protected void initializeChildrenFromInner() {
        crossConnectionPeerings = new HashMap<>();
        if (innerModel().peerings() != null) {
            for (ExpressRouteCrossConnectionPeeringInner peering : innerModel().peerings()) {
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
            .serviceClient()
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
            .serviceClient()
            .getExpressRouteCrossConnections()
            .updateTagsAsync(resourceGroupName(), name(), new TagsObject().withTags(innerModel().tags()));
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
        return innerModel().primaryAzurePort();
    }

    @Override
    public String secondaryAzurePort() {
        return innerModel().secondaryAzurePort();
    }

    @Override
    public Integer stag() {
        return innerModel().stag();
    }

    @Override
    public String peeringLocation() {
        return innerModel().peeringLocation();
    }

    @Override
    public int bandwidthInMbps() {
        return ResourceManagerUtils.toPrimitiveInt(innerModel().bandwidthInMbps());
    }

    @Override
    public ExpressRouteCircuitReference expressRouteCircuit() {
        return innerModel().expressRouteCircuit();
    }

    @Override
    public ServiceProviderProvisioningState serviceProviderProvisioningState() {
        return innerModel().serviceProviderProvisioningState();
    }

    @Override
    public String serviceProviderNotes() {
        return innerModel().serviceProviderNotes();
    }

    @Override
    public String provisioningState() {
        return innerModel().provisioningState().toString();
    }

    @Override
    public Map<String, ExpressRouteCrossConnectionPeering> peeringsMap() {
        return Collections.unmodifiableMap(crossConnectionPeerings);
    }

    @Override
    public Update withServiceProviderProvisioningState(ServiceProviderProvisioningState state) {
        innerModel().withServiceProviderProvisioningState(state);
        return this;
    }

    @Override
    public Update withServiceProviderNotes(String notes) {
        innerModel().withServiceProviderNotes(notes);
        return this;
    }
}
