/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.dns.implementation;

import com.azure.management.dns.models.ZoneInner;
import com.azure.management.dns.models.ZonesInner;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.azure.management.dns.DnsZone;
import com.azure.management.dns.DnsZones;
import reactor.core.publisher.Mono;

/**
 * Implementation of DnsZones.
 */
class DnsZonesImpl extends TopLevelModifiableResourcesImpl<
        DnsZone,
        DnsZoneImpl,
        ZoneInner,
        ZonesInner,
        DnsZoneManager>
        implements DnsZones {

    DnsZonesImpl(final DnsZoneManager dnsZoneManager) {
        super(dnsZoneManager.inner().zones(), dnsZoneManager);
    }

    @Override
    public DnsZoneImpl define(String name) {
        return setDefaults(wrapModel(name));
    }

    @Override
    protected DnsZoneImpl wrapModel(String name) {
        return new DnsZoneImpl(name, new ZoneInner(), this.manager());
    }

    @Override
    protected DnsZoneImpl wrapModel(ZoneInner inner) {
        if (inner == null) {
            return null;
        }
        return new DnsZoneImpl(inner.getName(), inner, this.manager());
    }

    private DnsZoneImpl setDefaults(DnsZoneImpl dnsZone) {
        // Zone location must be 'global' irrespective of region of the resource group it resides.
        dnsZone.inner().setLocation("global");
        return dnsZone;
    }

    @Override
    public Mono<Void> deleteByResourceGroupNameAsync(String resourceGroupName, String zoneName, String eTagValue) {
        return this.manager().inner().zones().deleteAsync(resourceGroupName, zoneName, eTagValue);
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id, String eTagValue) {
        return deleteByResourceGroupNameAsync(ResourceUtils.groupFromResourceId(id),
                ResourceUtils.nameFromResourceId(id),
                eTagValue);
    }

    @Override
    public void deleteByResourceGroupName(String resourceGroupName, String zoneName, String eTagValue) {
        deleteByResourceGroupNameAsync(resourceGroupName, zoneName, eTagValue).block();
    }

    @Override
    public void deleteById(String id, String eTagValue) {
        deleteByIdAsync(id, eTagValue).block();
    }
}
