/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.IpsecPolicy;
import com.microsoft.azure.management.network.LocalNetworkGateway;
import com.microsoft.azure.management.network.TunnelConnectionHealth;
import com.microsoft.azure.management.network.VirtualNetworkGateway;
import com.microsoft.azure.management.network.VirtualNetworkGatewayConnection;
import com.microsoft.azure.management.network.VirtualNetworkGatewayConnectionStatus;
import com.microsoft.azure.management.network.VirtualNetworkGatewayConnectionType;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import rx.Observable;

import java.util.List;


/**
 * Implementation for VirtualNetworkGatewayConnection and its create and update interfaces.
 */
@LangDefinition
public class VirtualNetworkGatewayConnectionImpl
        extends GroupableResourceImpl<VirtualNetworkGatewayConnection, VirtualNetworkGatewayConnectionInner, VirtualNetworkGatewayConnectionImpl, NetworkManager>
        implements VirtualNetworkGatewayConnection,
            VirtualNetworkGatewayConnection.Definition,
            VirtualNetworkGatewayConnection.Update {
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
    public List<TunnelConnectionHealth> tunnelConnectionStatus() {
        return inner().tunnelConnectionStatus();
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
    public SubResource peer() {
        return inner().peer();
    }

    @Override
    public Boolean isBgpEnabled() {
        return inner().enableBgp();
    }

    @Override
    public Boolean usePolicyBasedTrafficSelectors() {
        return inner().usePolicyBasedTrafficSelectors();
    }

    @Override
    public List<IpsecPolicy> ipsecPolicies() {
        return inner().ipsecPolicies();
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
    public VirtualNetworkGatewayConnectionImpl withExpressRoute() {
        inner().withConnectionType(VirtualNetworkGatewayConnectionType.EXPRESS_ROUTE);
        return this;
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl withLocalNetworkGateway(LocalNetworkGateway localNetworkGateway) {
        SubResource localNetworkGatewayRef = new SubResource().withId(localNetworkGateway.id());
        inner().withLocalNetworkGateway2(localNetworkGatewayRef);
        return this;
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl withSecondVirtualNetworkGateway(VirtualNetworkGateway virtualNetworkGateway2) {
        SubResource virtualNetworkGateway2Ref = new SubResource().withId(virtualNetworkGateway2.id());
        inner().withVirtualNetworkGateway2(virtualNetworkGateway2Ref);
        return this;
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl withSharedKey(String sharedKey) {
        inner().withSharedKey(sharedKey);
        return this;
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl enableBGP() {
        inner().withEnableBgp(true);
        return this;
    }

    @Override
    public VirtualNetworkGatewayConnectionImpl disableBGP() {
        inner().withEnableBgp(false);
        return this;
    }

    @Override
    protected Observable<VirtualNetworkGatewayConnectionInner> getInnerAsync() {
        return myManager.inner().virtualNetworkGatewayConnections().getByResourceGroupAsync(resourceGroupName(), name());
    }

    @Override
    public Observable<VirtualNetworkGatewayConnection> createResourceAsync() {
        beforeCreating();
        return myManager.inner().virtualNetworkGatewayConnections().createOrUpdateAsync(
                this.resourceGroupName(), this.name(), this.inner())
                .map(innerToFluentMap(this));
    }

    private void beforeCreating() {
        SubResource virtualNetworkGatewayRef = new SubResource().withId(parent.id());
        inner().withVirtualNetworkGateway1(virtualNetworkGatewayRef);
    }
}
