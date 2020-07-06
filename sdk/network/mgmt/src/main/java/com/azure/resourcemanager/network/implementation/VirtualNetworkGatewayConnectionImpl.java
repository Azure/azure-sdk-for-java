// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.ExpressRouteCircuit;
import com.azure.resourcemanager.network.models.IpsecPolicy;
import com.azure.resourcemanager.network.models.LocalNetworkGateway;
import com.azure.resourcemanager.network.models.TunnelConnectionHealth;
import com.azure.resourcemanager.network.models.VirtualNetworkGateway;
import com.azure.resourcemanager.network.models.VirtualNetworkGatewayConnection;
import com.azure.resourcemanager.network.models.VirtualNetworkGatewayConnectionStatus;
import com.azure.resourcemanager.network.models.VirtualNetworkGatewayConnectionType;
import com.azure.resourcemanager.network.models.AppliableWithTags;
import com.azure.resourcemanager.network.fluent.inner.VirtualNetworkGatewayConnectionInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
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
        return inner().authorizationKey();
    }

    @Override
    public String virtualNetworkGateway1Id() {
        if (inner().virtualNetworkGateway1() == null) {
            return null;
        }
        return inner().virtualNetworkGateway1().id();
    }

    @Override
    public String virtualNetworkGateway2Id() {
        if (inner().virtualNetworkGateway2() == null) {
            return null;
        }
        return inner().virtualNetworkGateway2().id();
    }

    @Override
    public String localNetworkGateway2Id() {
        if (inner().localNetworkGateway2() == null) {
            return null;
        }
        return inner().localNetworkGateway2().id();
    }

    @Override
    public VirtualNetworkGatewayConnectionType connectionType() {
        return inner().connectionType();
    }

    @Override
    public int routingWeight() {
        return Utils.toPrimitiveInt(inner().routingWeight());
    }

    @Override
    public String sharedKey() {
        return inner().sharedKey();
    }

    @Override
    public VirtualNetworkGatewayConnectionStatus connectionStatus() {
        return inner().connectionStatus();
    }

    @Override
    public Collection<TunnelConnectionHealth> tunnelConnectionStatus() {
        return Collections.unmodifiableCollection(inner().tunnelConnectionStatus());
    }

    @Override
    public long egressBytesTransferred() {
        return Utils.toPrimitiveLong(inner().egressBytesTransferred());
    }

    @Override
    public long ingressBytesTransferred() {
        return Utils.toPrimitiveLong(inner().ingressBytesTransferred());
    }

    @Override
    public String peerId() {
        return inner().peer() == null ? null : inner().peer().id();
    }

    @Override
    public boolean isBgpEnabled() {
        return Utils.toPrimitiveBoolean(inner().enableBgp());
    }

    @Override
    public boolean usePolicyBasedTrafficSelectors() {
        return Utils.toPrimitiveBoolean(inner().usePolicyBasedTrafficSelectors());
    }

    @Override
    public Collection<IpsecPolicy> ipsecPolicies() {
        return Collections.unmodifiableCollection(inner().ipsecPolicies());
    }

    @Override
    public String provisioningState() {
        return inner().provisioningState().toString();
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl withSiteToSite() {
        inner().withConnectionType(VirtualNetworkGatewayConnectionType.IPSEC);
        return this;
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl withVNetToVNet() {
        inner().withConnectionType(VirtualNetworkGatewayConnectionType.VNET2VNET);
        return this;
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl withExpressRoute(String circuitId) {
        inner().withConnectionType(VirtualNetworkGatewayConnectionType.EXPRESS_ROUTE);
        inner().withPeer(new SubResource().withId(circuitId));
        return this;
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl withExpressRoute(ExpressRouteCircuit circuit) {
        return withExpressRoute(circuit.id());
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl withLocalNetworkGateway(LocalNetworkGateway localNetworkGateway) {
        inner().withLocalNetworkGateway2(localNetworkGateway.inner());
        return this;
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl withSecondVirtualNetworkGateway(
        VirtualNetworkGateway virtualNetworkGateway2) {
        inner().withVirtualNetworkGateway2(virtualNetworkGateway2.inner());
        return this;
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl withSharedKey(String sharedKey) {
        inner().withSharedKey(sharedKey);
        return this;
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl withBgp() {
        inner().withEnableBgp(true);
        return this;
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl withoutBgp() {
        inner().withEnableBgp(false);
        return this;
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl withAuthorization(String authorizationKey) {
        inner().withAuthorizationKey(authorizationKey);
        return this;
    }

    @Override
    protected Mono<VirtualNetworkGatewayConnectionInner> getInnerAsync() {
        return myManager
            .inner()
            .getVirtualNetworkGatewayConnections()
            .getByResourceGroupAsync(resourceGroupName(), name());
    }

    @Override
    public Mono<VirtualNetworkGatewayConnection> createResourceAsync() {
        beforeCreating();
        return myManager
            .inner()
            .getVirtualNetworkGatewayConnections()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner())
            .map(innerToFluentMap(this));
    }

    private void beforeCreating() {
        inner().withVirtualNetworkGateway1(parent.inner());
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
            .inner()
            .getVirtualNetworkGatewayConnections()
            .updateTagsAsync(resourceGroupName(), name(), inner().tags())
            .flatMap(inner -> refreshAsync());
    }
}
