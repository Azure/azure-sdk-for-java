/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ExpressRouteCircuit;
import com.microsoft.azure.management.network.ExpressRouteCircuitPeering;
import com.microsoft.azure.management.network.ExpressRouteCircuitPeerings;
import com.microsoft.azure.management.network.ExpressRouteCircuitServiceProviderProperties;
import com.microsoft.azure.management.network.ExpressRouteCircuitSku;
import com.microsoft.azure.management.network.ExpressRouteCircuitSkuFamily;
import com.microsoft.azure.management.network.ExpressRouteCircuitSkuTier;
import com.microsoft.azure.management.network.ServiceProviderProvisioningState;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableParentResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import rx.Observable;

import java.util.HashMap;
import java.util.Map;

@LangDefinition
class ExpressRouteCircuitImpl extends GroupableParentResourceImpl<
        ExpressRouteCircuit,
        ExpressRouteCircuitInner,
        ExpressRouteCircuitImpl,
        NetworkManager>
        implements
        ExpressRouteCircuit,
        ExpressRouteCircuit.Definition,
        ExpressRouteCircuit.Update {
    private static final String SKU_DELIMITER = "_";
    private ExpressRouteCircuitPeeringsImpl peerings;
    private Map<String, ExpressRouteCircuitPeering> expressRouteCircuitPeerings;

    ExpressRouteCircuitImpl(String name, ExpressRouteCircuitInner innerObject, NetworkManager manager) {
        super(name, innerObject, manager);
        initializeChildrenFromInner();
    }

    @Override
    public ExpressRouteCircuitImpl withServiceProvidet(String serviceProviderName) {
        ensureServiceProviderProperties().withServiceProviderName(serviceProviderName);
        return this;
    }

    @Override
    public ExpressRouteCircuitImpl withPeeringLocation(String location) {
        ensureServiceProviderProperties().withPeeringLocation(location);
        return this;
    }

    @Override
    public ExpressRouteCircuitImpl withBandwidthInMbps(int bandwidthInMbps) {
        ensureServiceProviderProperties().withBandwidthInMbps(bandwidthInMbps);
        return this;
    }

    @Override
    public ExpressRouteCircuitImpl withSkuTier(ExpressRouteCircuitSkuTier skuTier) {
        ensureSku().withTier(skuTier).withName(skuTier.toString());
        return this;
    }


    @Override
    public ExpressRouteCircuitImpl enableClassicOperations() {
        inner().withAllowClassicOperations(true);
        return this;
    }

    @Override
    public Update disableClassicOperations() {
        inner().withAllowClassicOperations(false);
        return this;
    }

    private ExpressRouteCircuitServiceProviderProperties ensureServiceProviderProperties() {
        if (inner().serviceProviderProperties() == null) {
            inner().withServiceProviderProperties(new ExpressRouteCircuitServiceProviderProperties());
        }
        return inner().serviceProviderProperties();
    }

    private ExpressRouteCircuitSku ensureSku() {
        if (inner().sku() == null) {
            inner().withSku(new ExpressRouteCircuitSku());
        }
        return inner().sku();
    }

    @Override
    public ExpressRouteCircuitImpl withSkuFamily(ExpressRouteCircuitSkuFamily skuFamily) {
        ensureSku().withFamily(skuFamily);
        return this;
    }

    protected void beforeCreating() {
        ExpressRouteCircuitSku sku = inner().sku();
        ensureSku().withName((sku.tier() == null ? "" : sku.tier().toString()) + SKU_DELIMITER + (sku.family() == null ? "" : sku.family().toString()));
    }

    @Override
    protected void afterCreating() {
    }

    @Override
    protected Observable<ExpressRouteCircuitInner> createInner() {
        return this.manager().inner().expressRouteCircuits().createOrUpdateAsync(
                this.resourceGroupName(), this.name(), this.inner());
    }

    @Override
    protected void initializeChildrenFromInner() {
        expressRouteCircuitPeerings = new HashMap<>();
        if (inner().peerings() != null) {
            for (ExpressRouteCircuitPeeringInner peering : inner().peerings()) {
                expressRouteCircuitPeerings.put(peering.name(),
                        new ExpressRouteCircuitPeeringImpl(this, peering, manager().inner().expressRouteCircuitPeerings(), peering.peeringType()));
            }
        }
    }

    @Override
    protected Observable<ExpressRouteCircuitInner> getInnerAsync() {
        return this.manager().inner().expressRouteCircuits().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    // Getters

    @Override
    public ExpressRouteCircuitPeerings peerings() {
        if (peerings == null) {
            peerings = new ExpressRouteCircuitPeeringsImpl(this);
        }
        return peerings;
    }

    @Override
    public ExpressRouteCircuitSku sku() {
        return inner().sku();
    }

    @Override
    public boolean isAllowClassicOperations() {
        return Utils.toPrimitiveBoolean(inner().allowClassicOperations());
    }

    @Override
    public String circuitProvisioningState() {
        return inner().circuitProvisioningState();
    }

    @Override
    public ServiceProviderProvisioningState serviceProviderProvisioningState() {
        return inner().serviceProviderProvisioningState();
    }

    @Override
    public String serviceKey() {
        return inner().serviceKey();
    }

    @Override
    public String serviceProviderNotes() {
        return inner().serviceProviderNotes();
    }

    @Override
    public ExpressRouteCircuitServiceProviderProperties serviceProviderProperties() {
        return inner().serviceProviderProperties();
    }

    @Override
    public String provisioningState() {
        return inner().provisioningState();
    }

    @Override
    public Map<String, ExpressRouteCircuitPeering> peeringsMap() {
        return expressRouteCircuitPeerings;
    }
}
