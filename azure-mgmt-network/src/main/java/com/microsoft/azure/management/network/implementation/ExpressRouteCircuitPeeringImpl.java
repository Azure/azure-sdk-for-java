/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ExpressRouteCircuit;
import com.microsoft.azure.management.network.ExpressRouteCircuitPeering;
import com.microsoft.azure.management.network.ExpressRouteCircuitPeeringConfig;
import com.microsoft.azure.management.network.ExpressRouteCircuitPeeringState;
import com.microsoft.azure.management.network.ExpressRouteCircuitPeeringType;
import com.microsoft.azure.management.network.Ipv6ExpressRouteCircuitPeeringConfig;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import rx.Observable;
import rx.functions.Func1;

import java.util.Arrays;

@LangDefinition
class ExpressRouteCircuitPeeringImpl extends
        CreatableUpdatableImpl<ExpressRouteCircuitPeering, ExpressRouteCircuitPeeringInner, ExpressRouteCircuitPeeringImpl>
        implements
        ExpressRouteCircuitPeering,
        ExpressRouteCircuitPeering.Definition,
        ExpressRouteCircuitPeering.Update {
    private final ExpressRouteCircuitPeeringsInner client;
    private final ExpressRouteCircuit parent;
    private ExpressRouteCircuitStatsImpl stats;

    ExpressRouteCircuitPeeringImpl(ExpressRouteCircuitImpl parent, ExpressRouteCircuitPeeringInner innerObject,
                                   ExpressRouteCircuitPeeringsInner client, ExpressRouteCircuitPeeringType type) {
        super(type.toString(), innerObject);
        this.client = client;
        this.parent = parent;
        this.stats = new ExpressRouteCircuitStatsImpl(innerObject.stats());
        inner().withPeeringType(type);
    }

    @Override
    public ExpressRouteCircuitPeeringImpl withAdvertisedPublicPrefixes(String publicPrefix) {
        ensureMicrosoftPeeringConfig().withAdvertisedPublicPrefixes(Arrays.asList(publicPrefix));
        return this;
    }

    private ExpressRouteCircuitPeeringConfig ensureMicrosoftPeeringConfig() {
        if (inner().microsoftPeeringConfig() == null) {
            inner().withMicrosoftPeeringConfig(new ExpressRouteCircuitPeeringConfig());
        }
        return inner().microsoftPeeringConfig();
    }

    @Override
    public ExpressRouteCircuitPeeringImpl withPrimaryPeerAddressPrefix(String addressPrefix) {
        inner().withPrimaryPeerAddressPrefix(addressPrefix);
        return this;
    }

    @Override
    public ExpressRouteCircuitPeeringImpl withSecondaryPeerAddressPrefix(String addressPrefix) {
        inner().withSecondaryPeerAddressPrefix(addressPrefix);
        return this;
    }

    @Override
    public ExpressRouteCircuitPeeringImpl withVlanId(int vlanId) {
        inner().withVlanId(vlanId);
        return this;
    }

    @Override
    public ExpressRouteCircuitPeeringImpl withPeerAsn(int peerAsn) {
        inner().withPeerASN(peerAsn);
        return this;
    }

    @Override
    protected Observable<ExpressRouteCircuitPeeringInner> getInnerAsync() {
        return this.client.getAsync(parent.resourceGroupName(), parent.name(), name());
    }

    @Override
    public boolean isInCreateMode() {
        return this.inner().id() == null;
    }

    @Override
    public Observable<ExpressRouteCircuitPeering> createResourceAsync() {
        return this.client.createOrUpdateAsync(parent.resourceGroupName(), parent.name(), this.name(), inner())
                .map(new Func1<ExpressRouteCircuitPeeringInner, ExpressRouteCircuitPeering>() {
                    @Override
                    public ExpressRouteCircuitPeering call(ExpressRouteCircuitPeeringInner innerModel) {
                        ExpressRouteCircuitPeeringImpl.this.setInner(innerModel);
                        stats = new ExpressRouteCircuitStatsImpl(innerModel.stats());
                        return ExpressRouteCircuitPeeringImpl.this;
                    }
                });
    }

    // Getters

    @Override
    public String id() {
        return inner().id();
    }

    @Override
    public ExpressRouteCircuitPeeringType peeringType() {
        return inner().peeringType();
    }

    @Override
    public ExpressRouteCircuitPeeringState state() {
        return inner().state();
    }

    @Override
    public int azureASN() {
        return Utils.toPrimitiveInt(inner().azureASN());
    }

    @Override
    public int peerASN() {
        return Utils.toPrimitiveInt(inner().peerASN());
    }

    @Override
    public String primaryPeerAddressPrefix() {
        return inner().primaryPeerAddressPrefix();
    }

    @Override
    public String secondaryPeerAddressPrefix() {
        return inner().secondaryPeerAddressPrefix();
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
    public String sharedKey() {
        return inner().sharedKey();
    }

    @Override
    public int vlanId() {
        return Utils.toPrimitiveInt(inner().vlanId());
    }

    @Override
    public ExpressRouteCircuitPeeringConfig microsoftPeeringConfig() {
        return inner().microsoftPeeringConfig();
    }

    @Override
    public ExpressRouteCircuitStatsImpl stats() {
        return stats;
    }

    @Override
    public String provisioningState() {
        return inner().provisioningState();
    }

    @Override
    public String lastModifiedBy() {
        return inner().lastModifiedBy();
    }

    @Override
    public Ipv6ExpressRouteCircuitPeeringConfig ipv6PeeringConfig() {
        return inner().ipv6PeeringConfig();
    }

    @Override
    public NetworkManager manager() {
        return parent.manager();
    }

    @Override
    public String resourceGroupName() {
        return parent.resourceGroupName();
    }
}
