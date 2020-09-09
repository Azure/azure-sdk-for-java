// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.models.ExpressRouteCircuitPeeringConfig;
import com.azure.resourcemanager.network.models.ExpressRouteCrossConnectionPeering;
import com.azure.resourcemanager.network.models.Ipv6ExpressRouteCircuitPeeringConfig;
import com.azure.resourcemanager.network.models.Ipv6PeeringConfig;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import java.util.ArrayList;
import java.util.List;

/** Implementation for Ipv6PeeringConfig. */
class Ipv6PeeringConfigImpl extends IndexableWrapperImpl<Ipv6ExpressRouteCircuitPeeringConfig>
    implements Ipv6PeeringConfig,
        Ipv6PeeringConfig.Definition<ExpressRouteCrossConnectionPeering.DefinitionStages.WithCreate>,
        Ipv6PeeringConfig.UpdateDefinition<ExpressRouteCrossConnectionPeering.Update>,
        Ipv6PeeringConfig.Update {
    private final ExpressRouteCrossConnectionPeeringImpl parent;

    Ipv6PeeringConfigImpl(
        Ipv6ExpressRouteCircuitPeeringConfig innerObject, ExpressRouteCrossConnectionPeeringImpl parent) {
        super(innerObject);
        this.parent = parent;
    }

    @Override
    public Ipv6PeeringConfigImpl withAdvertisedPublicPrefixes(List<String> publicPrefixes) {
        ensureMicrosoftPeeringConfig().withAdvertisedPublicPrefixes(publicPrefixes);
        return this;
    }

    @Override
    public Ipv6PeeringConfigImpl withAdvertisedPublicPrefix(String publicPrefix) {
        ExpressRouteCircuitPeeringConfig peeringConfig = ensureMicrosoftPeeringConfig();
        if (peeringConfig.advertisedPublicPrefixes() == null) {
            peeringConfig.withAdvertisedPublicPrefixes(new ArrayList<String>());
        }
        peeringConfig.advertisedPublicPrefixes().add(publicPrefix);
        return this;
    }

    @Override
    public Ipv6PeeringConfigImpl withPrimaryPeerAddressPrefix(String addressPrefix) {
        inner().withPrimaryPeerAddressPrefix(addressPrefix);
        return this;
    }

    @Override
    public Ipv6PeeringConfigImpl withSecondaryPeerAddressPrefix(String addressPrefix) {
        inner().withSecondaryPeerAddressPrefix(addressPrefix);
        return this;
    }

    @Override
    public Ipv6PeeringConfigImpl withCustomerAsn(int customerASN) {
        ensureMicrosoftPeeringConfig().withCustomerAsn(customerASN);
        return this;
    }

    @Override
    public Ipv6PeeringConfigImpl withRouteFilter(String routeFilterId) {
        inner().withRouteFilter(new SubResource().withId(routeFilterId));
        return this;
    }

    @Override
    public Ipv6PeeringConfigImpl withoutRouteFilter() {
        inner().withRouteFilter(null);
        return this;
    }

    @Override
    public Ipv6PeeringConfigImpl withRoutingRegistryName(String routingRegistryName) {
        ensureMicrosoftPeeringConfig().withRoutingRegistryName(routingRegistryName);
        return this;
    }

    private ExpressRouteCircuitPeeringConfig ensureMicrosoftPeeringConfig() {
        if (inner().microsoftPeeringConfig() == null) {
            inner().withMicrosoftPeeringConfig(new ExpressRouteCircuitPeeringConfig());
        }
        return inner().microsoftPeeringConfig();
    }

    @Override
    public ExpressRouteCrossConnectionPeeringImpl attach() {
        return parent.attachIpv6Config(this);
    }

    @Override
    public ExpressRouteCrossConnectionPeeringImpl parent() {
        return parent;
    }
}
