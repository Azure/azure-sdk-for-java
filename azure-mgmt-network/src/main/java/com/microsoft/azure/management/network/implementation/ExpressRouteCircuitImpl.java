/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ExpressRouteCircuit;
import com.microsoft.azure.management.network.ExpressRouteCircuitServiceProviderProperties;
import com.microsoft.azure.management.network.ExpressRouteCircuitSku;
import com.microsoft.azure.management.network.ExpressRouteCircuitSkuFamily;
import com.microsoft.azure.management.network.ExpressRouteCircuitSkuTier;
import com.microsoft.azure.management.network.ServiceProviderProvisioningState;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import rx.Observable;

@LangDefinition
class ExpressRouteCircuitImpl extends GroupableResourceImpl<
        ExpressRouteCircuit,
        ExpressRouteCircuitInner,
        ExpressRouteCircuitImpl,
        NetworkManager>
        implements
        ExpressRouteCircuit,
        ExpressRouteCircuit.Definition,
        ExpressRouteCircuit.Update {
    private static final String SKU_DELIMITER = "_";

    protected ExpressRouteCircuitImpl(String name, ExpressRouteCircuitInner innerObject, NetworkManager manager) {
        super(name, innerObject, manager);
    }

    @Override
    public ExpressRouteCircuitImpl withServiceProvidet(String serviceProviderName) {
        ensureServiceProviderProperties().withServiceProviderName(serviceProviderName);
        return this;
    }

    private ExpressRouteCircuitServiceProviderProperties ensureServiceProviderProperties() {
        if (inner().serviceProviderProperties() == null) {
            inner().withServiceProviderProperties(new ExpressRouteCircuitServiceProviderProperties());
        }
        return inner().serviceProviderProperties();
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

    private ExpressRouteCircuitSku ensureSku() {
        if (inner().sku() == null) {
            inner().withSku(new ExpressRouteCircuitSku());
        }
        return inner().sku();
    }

    private void beforeCreating() {
        ExpressRouteCircuitSku sku = inner().sku();
        ensureSku().withName((sku.tier() == null ? "" : sku.tier().toString()) + SKU_DELIMITER + (sku.family() == null ? "" : sku.family().toString()));
    }

    @Override
    public ExpressRouteCircuit.DefinitionStages.WithCreate withSkuFamily(ExpressRouteCircuitSkuFamily skuFamily) {
        ensureSku().withFamily(skuFamily);
        return this;
    }

    @Override
    public Observable<ExpressRouteCircuit> createResourceAsync() {
        beforeCreating();
        return this.manager().inner().expressRouteCircuits().createOrUpdateAsync(
                this.resourceGroupName(), this.name(), this.inner())
                .map(innerToFluentMap(this));
    }

    @Override
    protected Observable<ExpressRouteCircuitInner> getInnerAsync() {
        return this.manager().inner().expressRouteCircuits().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    // Getters

    @Override
    public ExpressRouteCircuitSku getSku() {
        return inner().sku();
    }

    @Override
    public boolean isAllowClassicOperations() {
        return Utils.toPrimitiveBoolean(inner().allowClassicOperations());
    }

    @Override
    public String getCircuitProvisioningState() {
        return inner().circuitProvisioningState();
    }

    @Override
    public ServiceProviderProvisioningState getServiceProviderProvisioningState() {
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
}
