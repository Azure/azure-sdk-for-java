/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.management.network.ExpressRouteCircuit;
import com.azure.management.network.IpsecPolicy;
import com.azure.management.network.LocalNetworkGateway;
import com.azure.management.network.TunnelConnectionHealth;
import com.azure.management.network.VirtualNetworkGateway;
import com.azure.management.network.VirtualNetworkGatewayConnection;
import com.azure.management.network.VirtualNetworkGatewayConnectionStatus;
import com.azure.management.network.VirtualNetworkGatewayConnectionType;
import com.azure.management.network.models.AppliableWithTags;
import com.azure.management.network.models.VirtualNetworkGatewayConnectionInner;
import com.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.management.resources.fluentcore.utils.Utils;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;


/**
 * Implementation for VirtualNetworkGatewayConnection and its create and update interfaces.
 */
public class VirtualNetworkGatewayConnectionImpl
        extends GroupableResourceImpl<VirtualNetworkGatewayConnection, VirtualNetworkGatewayConnectionInner, VirtualNetworkGatewayConnectionImpl, NetworkManager>
        implements VirtualNetworkGatewayConnection,
        VirtualNetworkGatewayConnection.Definition,
        VirtualNetworkGatewayConnection.Update,
        AppliableWithTags<VirtualNetworkGatewayConnection> {
    private final VirtualNetworkGateway parent;

    VirtualNetworkGatewayConnectionImpl(String name,
                                        VirtualNetworkGatewayImpl parent,
                                        VirtualNetworkGatewayConnectionInner inner) {
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
        return inner().virtualNetworkGateway1().getId();
    }

    @Override
    public String virtualNetworkGateway2Id() {
        if (inner().virtualNetworkGateway2() == null) {
            return null;
        }
        return inner().virtualNetworkGateway2().getId();
    }

    @Override
    public String localNetworkGateway2Id() {
        if (inner().localNetworkGateway2() == null) {
            return null;
        }
        return inner().localNetworkGateway2().getId();
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
        return inner().peer() == null ? null : inner().peer().getId();
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
        return inner().provisioningState();
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
        inner().withPeer(new SubResource().setId(circuitId));
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
    public VirtualNetworkGatewayConnectionImpl withSecondVirtualNetworkGateway(VirtualNetworkGateway virtualNetworkGateway2) {
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
        return myManager.inner().virtualNetworkGatewayConnections().getByResourceGroupAsync(resourceGroupName(), name());
    }

    @Override
    public Mono<VirtualNetworkGatewayConnection> createResourceAsync() {
        beforeCreating();
        return myManager.inner().virtualNetworkGatewayConnections().createOrUpdateAsync(
                this.resourceGroupName(), this.name(), this.inner())
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
        return this.manager().inner().virtualNetworkGatewayConnections().updateTagsAsync(resourceGroupName(), name(), inner().getTags())
                .flatMap(inner -> refreshAsync());
    }
}
