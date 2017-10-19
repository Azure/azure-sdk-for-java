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
import com.microsoft.azure.management.network.ExpressRouteCircuitPeeringType;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import rx.Observable;

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

    ExpressRouteCircuitPeeringImpl(ExpressRouteCircuitImpl parent, ExpressRouteCircuitPeeringInner innerObject,
                                   ExpressRouteCircuitPeeringsInner client, ExpressRouteCircuitPeeringType type) {
        super(type.toString(), innerObject);
        this.client = client;
        this.parent = parent;
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
    public DefinitionStages.WithCreate withPeerAsn(int peerAsn) {
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
                .map(innerToFluentMap(this));
    }

    @Override
    public String id() {
        return inner().id();
    }

    @Override
    public ExpressRouteCircuit parent() {
        return parent;
    }
}
