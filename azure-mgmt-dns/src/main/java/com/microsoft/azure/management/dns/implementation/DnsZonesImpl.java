/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.DnsZones;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ListableResourcesImpl;
import rx.Completable;
import rx.Observable;

/**
 * Implementation of DnsZones.
 */
@LangDefinition
class DnsZonesImpl extends ListableResourcesImpl<
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
    public PagedList<DnsZone> list() {
        return wrapList(this.inner().list());
    }

    @Override
    public PagedList<DnsZone> listByGroup(String groupName) {
        return wrapList(this.inner().listByResourceGroup(groupName));
    }

    @Override
    protected Observable<ZoneInner> getAsync(String resourceGroupName, String name) {
        return this.inner().getAsync(resourceGroupName, name);
    }

    @Override
    public Completable deleteByGroupAsync(String groupName, String name) {
        return this.inner().deleteAsync(groupName, name).toCompletable();
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
        return new DnsZoneImpl(inner.name(), inner, this.manager());
    }

    private DnsZoneImpl setDefaults(DnsZoneImpl dnsZone) {
        // Zone location must be 'global' irrespective of region of the resource group it resides.
        dnsZone.inner().withLocation("global");
        return dnsZone;
    }
}
