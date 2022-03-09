// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.ConnectionSharedKeyInner;
import com.azure.resourcemanager.network.models.ExpressRouteCircuit;
import com.azure.resourcemanager.network.models.IpsecPolicy;
import com.azure.resourcemanager.network.models.LocalNetworkGateway;
import com.azure.resourcemanager.network.models.TagsObject;
import com.azure.resourcemanager.network.models.TunnelConnectionHealth;
import com.azure.resourcemanager.network.models.VirtualNetworkGateway;
import com.azure.resourcemanager.network.models.VirtualNetworkGatewayConnection;
import com.azure.resourcemanager.network.models.VirtualNetworkGatewayConnectionStatus;
import com.azure.resourcemanager.network.models.VirtualNetworkGatewayConnectionType;
import com.azure.resourcemanager.network.models.AppliableWithTags;
import com.azure.resourcemanager.network.fluent.models.VirtualNetworkGatewayConnectionInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;

import java.util.Collection;
import java.util.Collections;
import reactor.core.publisher.Mono;

/** Implementation for VirtualNetworkGatewayConnection and its create and update interfaces. */
public class VirtualNetworkGatewayConnectionImpl
    extends GroupableResourceImpl<
        VirtualNetworkGatewayConnection,
        VirtualNetworkGatewayConnectionInner,
        VirtualNetworkGatewayConnectionImpl,
        NetworkManager>
    implements VirtualNetworkGatewayConnection,
        VirtualNetworkGatewayConnection.Definition,
        VirtualNetworkGatewayConnection.Update,
        AppliableWithTags<VirtualNetworkGatewayConnection> {
    private final VirtualNetworkGateway parent;
    private String updateSharedKey;

    VirtualNetworkGatewayConnectionImpl(
        String name, VirtualNetworkGatewayImpl parent, VirtualNetworkGatewayConnectionInner inner) {
        super(name, inner, parent.manager());
        this.parent = parent;
    }

    @Override
    public VirtualNetworkGateway parent() {
        return parent;
    }

    @Override
    public String authorizationKey() {
        return innerModel().authorizationKey();
    }

    @Override
    public String virtualNetworkGateway1Id() {
        if (innerModel().virtualNetworkGateway1() == null) {
            return null;
        }
        return innerModel().virtualNetworkGateway1().id();
    }

    @Override
    public String virtualNetworkGateway2Id() {
        if (innerModel().virtualNetworkGateway2() == null) {
            return null;
        }
        return innerModel().virtualNetworkGateway2().id();
    }

    @Override
    public String localNetworkGateway2Id() {
        if (innerModel().localNetworkGateway2() == null) {
            return null;
        }
        return innerModel().localNetworkGateway2().id();
    }

    @Override
    public VirtualNetworkGatewayConnectionType connectionType() {
        return innerModel().connectionType();
    }

    @Override
    public int routingWeight() {
        return ResourceManagerUtils.toPrimitiveInt(innerModel().routingWeight());
    }

    @Override
    public String sharedKey() {
        return innerModel().sharedKey();
    }

    @Override
    public VirtualNetworkGatewayConnectionStatus connectionStatus() {
        return innerModel().connectionStatus();
    }

    @Override
    public Collection<TunnelConnectionHealth> tunnelConnectionStatus() {
        return Collections.unmodifiableCollection(innerModel().tunnelConnectionStatus());
    }

    @Override
    public long egressBytesTransferred() {
        return ResourceManagerUtils.toPrimitiveLong(innerModel().egressBytesTransferred());
    }

    @Override
    public long ingressBytesTransferred() {
        return ResourceManagerUtils.toPrimitiveLong(innerModel().ingressBytesTransferred());
    }

    @Override
    public String peerId() {
        return innerModel().peer() == null ? null : innerModel().peer().id();
    }

    @Override
    public boolean isBgpEnabled() {
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().enableBgp());
    }

    @Override
    public boolean usePolicyBasedTrafficSelectors() {
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().usePolicyBasedTrafficSelectors());
    }

    @Override
    public Collection<IpsecPolicy> ipsecPolicies() {
        return Collections.unmodifiableCollection(innerModel().ipsecPolicies());
    }

    @Override
    public String provisioningState() {
        return innerModel().provisioningState().toString();
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl withSiteToSite() {
        innerModel().withConnectionType(VirtualNetworkGatewayConnectionType.IPSEC);
        return this;
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl withVNetToVNet() {
        innerModel().withConnectionType(VirtualNetworkGatewayConnectionType.VNET2VNET);
        return this;
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl withExpressRoute(String circuitId) {
        innerModel().withConnectionType(VirtualNetworkGatewayConnectionType.EXPRESS_ROUTE);
        innerModel().withPeer(new SubResource().withId(circuitId));
        return this;
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl withExpressRoute(ExpressRouteCircuit circuit) {
        return withExpressRoute(circuit.id());
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl withLocalNetworkGateway(LocalNetworkGateway localNetworkGateway) {
        innerModel().withLocalNetworkGateway2(localNetworkGateway.innerModel());
        return this;
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl withSecondVirtualNetworkGateway(
        VirtualNetworkGateway virtualNetworkGateway2) {
        innerModel().withVirtualNetworkGateway2(virtualNetworkGateway2.innerModel());
        return this;
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl withSharedKey(String sharedKey) {
        if (isInCreateMode()) {
            innerModel().withSharedKey(sharedKey);
        } else {
            updateSharedKey = sharedKey;
        }
        return this;
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl withBgp() {
        innerModel().withEnableBgp(true);
        return this;
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl withoutBgp() {
        innerModel().withEnableBgp(false);
        return this;
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl withAuthorization(String authorizationKey) {
        innerModel().withAuthorizationKey(authorizationKey);
        return this;
    }

    @Override
    protected Mono<VirtualNetworkGatewayConnectionInner> getInnerAsync() {
        return myManager
            .serviceClient()
            .getVirtualNetworkGatewayConnections()
            .getByResourceGroupAsync(resourceGroupName(), name());
    }

    @Override
    public Mono<VirtualNetworkGatewayConnection> createResourceAsync() {
        beforeCreating();
        return myManager
            .serviceClient()
            .getVirtualNetworkGatewayConnections()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.innerModel())
            .map(innerToFluentMap(this))
            .flatMap(virtualNetworkGatewayConnection -> {
                if (updateSharedKey == null) {
                    return Mono.just(virtualNetworkGatewayConnection);
                }
                return myManager.serviceClient().getVirtualNetworkGatewayConnections()
                    .setSharedKeyAsync(
                        this.resourceGroupName(),
                        this.name(),
                        new ConnectionSharedKeyInner().withValue(updateSharedKey))
                    .doOnSuccess(inner -> {
                        updateSharedKey = null;
                    })
                    .then(myManager.serviceClient().getVirtualNetworkGatewayConnections()
                        .getByResourceGroupAsync(this.resourceGroupName(), this.name())
                        .map(innerToFluentMap(this)));
            });
    }

    private void beforeCreating() {
        innerModel().withVirtualNetworkGateway1(parent.innerModel());
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl updateTags() {
        return this;
    }

    @Override
    public VirtualNetworkGatewayConnection applyTags() {
        return applyTagsAsync().block();
    }

    @Override
    public Mono<VirtualNetworkGatewayConnection> applyTagsAsync() {
        return this
            .manager()
            .serviceClient()
            .getVirtualNetworkGatewayConnections()
            .updateTagsAsync(resourceGroupName(), name(), new TagsObject().withTags(innerModel().tags()))
            .flatMap(inner -> refreshAsync());
    }
}
