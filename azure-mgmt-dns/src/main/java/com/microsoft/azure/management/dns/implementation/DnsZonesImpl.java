package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.DnsZones;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import rx.Observable;
import rx.functions.Func1;

/**
 * Implementation of {@link DnsZones}.
 */
class DnsZonesImpl extends GroupableResourcesImpl<
        DnsZone,
        DnsZoneImpl,
        ZoneInner,
        ZonesInner,
        DnsZoneManager>
        implements DnsZones {
    private final RecordSetsInner recordSetsClient;

    DnsZonesImpl(
            final DnsManagementClientImpl dnsManagementClient,
            final DnsZoneManager dnsZoneManager) {
        super(dnsManagementClient.zones(), dnsZoneManager);
        this.recordSetsClient = dnsManagementClient.recordSets();
    }

    @Override
    public PagedList<DnsZone> list() {
        return wrapList(this.innerCollection.listInSubscription());
    }

    @Override
    public PagedList<DnsZone> listByGroup(String groupName) {
        return wrapList(this.innerCollection.listInResourceGroup(groupName));
    }

    @Override
    public DnsZone getByGroup(String groupName, String name) {
        return wrapModel(this.innerCollection.get(groupName, name));
    }

    @Override
    public Observable<Void> deleteByGroupAsync(String groupName, String name) {
        return this.innerCollection.deleteAsync(groupName, name)
            .map(new Func1<ZoneDeleteResultInner, Void>() {
                @Override
                public Void call(ZoneDeleteResultInner zoneDeleteResultInner) {
                    return null;
                }
            });
    }

    @Override
    public DnsZoneImpl define(String name) {
        return setDefaults(wrapModel(name));
    }

    @Override
    protected DnsZoneImpl wrapModel(String name) {
        return new DnsZoneImpl(name,
                new ZoneInner(),
                this.innerCollection,
                this.recordSetsClient,
                this.myManager);
    }

    @Override
    protected DnsZoneImpl wrapModel(ZoneInner inner) {
        return new DnsZoneImpl(inner.name(),
                inner,
                this.innerCollection,
                this.recordSetsClient,
                this.myManager);
    }

    private DnsZoneImpl setDefaults(DnsZoneImpl dnsZone) {
        // Zone location must be 'global' irrespective of region of the resource group it resides.
        dnsZone.inner().withLocation("global");
        return dnsZone;
    }
}
