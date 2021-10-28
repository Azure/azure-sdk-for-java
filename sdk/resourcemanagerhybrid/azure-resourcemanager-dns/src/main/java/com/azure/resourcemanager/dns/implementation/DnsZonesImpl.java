// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.implementation;

import com.azure.resourcemanager.dns.DnsZoneManager;
import com.azure.resourcemanager.dns.fluent.ZonesClient;
import com.azure.resourcemanager.dns.fluent.models.ZoneInner;
import com.azure.resourcemanager.dns.models.DnsZone;
import com.azure.resourcemanager.dns.models.DnsZones;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import reactor.core.publisher.Mono;

/** Implementation of DnsZones. */
public class DnsZonesImpl
    extends TopLevelModifiableResourcesImpl<DnsZone, DnsZoneImpl, ZoneInner, ZonesClient, DnsZoneManager>
    implements DnsZones {

    public DnsZonesImpl(final DnsZoneManager dnsZoneManager) {
        super(dnsZoneManager.serviceClient().getZones(), dnsZoneManager);
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
        return new DnsZoneImpl(inner.name(), inner, this.manager());
    }

    private DnsZoneImpl setDefaults(DnsZoneImpl dnsZone) {
        // Zone location must be 'global' irrespective of region of the resource group it resides.
        dnsZone.innerModel().withLocation("global");
        return dnsZone;
    }

    @Override
    public Mono<Void> deleteByResourceGroupNameAsync(String resourceGroupName, String zoneName) {
        return this.manager().serviceClient().getZones().deleteAsync(resourceGroupName, zoneName).then();
    }

    @Override
    public Mono<Void> deleteByResourceGroupNameAsync(String resourceGroupName, String zoneName, String eTagValue) {
        return this.manager().serviceClient().getZones().deleteAsync(resourceGroupName, zoneName, eTagValue).then();
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        return deleteByResourceGroupNameAsync(
            ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id, String eTagValue) {
        return deleteByResourceGroupNameAsync(
            ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id), eTagValue);
    }

    @Override
    public void deleteByResourceGroupName(String resourceGroupName, String zoneName) {
        deleteByResourceGroupNameAsync(resourceGroupName, zoneName).block();
    }

    @Override
    public void deleteByResourceGroupName(String resourceGroupName, String zoneName, String eTagValue) {
        deleteByResourceGroupNameAsync(resourceGroupName, zoneName, eTagValue).block();
    }

    @Override
    public void deleteById(String id) {
        deleteByIdAsync(id).block();
    }

    @Override
    public void deleteById(String id, String eTagValue) {
        deleteByIdAsync(id, eTagValue).block();
    }
}
