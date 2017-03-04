/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.dns.ARecordSets;
import com.microsoft.azure.management.dns.AaaaRecordSets;
import com.microsoft.azure.management.dns.CNameRecordSets;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.MXRecordSets;
import com.microsoft.azure.management.dns.NSRecordSets;
import com.microsoft.azure.management.dns.PtrRecordSets;
import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.dns.SoaRecordSet;
import com.microsoft.azure.management.dns.SrvRecordSets;
import com.microsoft.azure.management.dns.TxtRecordSets;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;

/**
 * Implementation for {@link DnsZone}.
 */
@LangDefinition
public class DnsZoneImpl
        extends GroupableResourceImpl<
            DnsZone,
            ZoneInner,
            DnsZoneImpl,
            DnsZoneManager>
        implements
            DnsZone,
            DnsZone.Definition,
            DnsZone.Update {

    private ARecordSets aRecordSets;
    private AaaaRecordSets aaaaRecordSets;
    private CNameRecordSets cnameRecordSets;
    private MXRecordSets mxRecordSets;
    private NSRecordSets nsRecordSets;
    private PtrRecordSets ptrRecordSets;
    private SrvRecordSets srvRecordSets;
    private TxtRecordSets txtRecordSets;
    private DnsRecordSetsImpl recordSetsImpl;

    DnsZoneImpl(String name, final ZoneInner innerModel, final DnsZoneManager manager) {
        super(name, innerModel, manager);
        this.recordSetsImpl = new DnsRecordSetsImpl(this);
        initRecordSets();
    }

    @Override
    public long maxNumberOfRecordSets() {
        return this.inner().maxNumberOfRecordSets();
    }

    @Override
    public long numberOfRecordSets() {
        return this.inner().numberOfRecordSets();
    }

    @Override
    public List<String> nameServers() {
        return this.inner().nameServers();
    }

    @Override
    public ARecordSets aRecordSets() {
        return this.aRecordSets;
    }

    @Override
    public AaaaRecordSets aaaaRecordSets() {
        return this.aaaaRecordSets;
    }

    @Override
    public CNameRecordSets cNameRecordSets() {
        return this.cnameRecordSets;
    }

    @Override
    public MXRecordSets mxRecordSets() {
        return this.mxRecordSets;
    }

    @Override
    public NSRecordSets nsRecordSets() {
        return this.nsRecordSets;
    }

    @Override
    public PtrRecordSets ptrRecordSets() {
        return this.ptrRecordSets;
    }

    @Override
    public SrvRecordSets srvRecordSets() {
        return this.srvRecordSets;
    }

    @Override
    public TxtRecordSets txtRecordSets() {
        return this.txtRecordSets;
    }

    @Override
    public SoaRecordSet getSoaRecordSet() {
        RecordSetInner inner = this.manager().inner().recordSets().get(this.resourceGroupName(), this.name(), "@", RecordType.SOA);
        return new SoaRecordSetImpl(this, inner);
    }

    // Setters

    @Override
    public DnsRecordSetImpl defineARecordSet(String name) {
        return recordSetsImpl.defineARecordSet(name);
    }

    @Override
    public DnsRecordSetImpl defineAaaaRecordSet(String name) {
        return recordSetsImpl.defineAaaaRecordSet(name);
    }

    @Override
    public DnsZoneImpl withCNameRecordSet(String name, String alias) {
        recordSetsImpl.withCNameRecordSet(name, alias);
        return this;
    }

    @Override
    public DnsRecordSetImpl defineMXRecordSet(String name) {
        return recordSetsImpl.defineMXRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl defineNSRecordSet(String name) {
        return recordSetsImpl.defineNSRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl definePtrRecordSet(String name) {
        return recordSetsImpl.definePtrRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl defineSrvRecordSet(String name) {
        return recordSetsImpl.defineSrvRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl defineTxtRecordSet(String name) {
        return recordSetsImpl.defineTxtRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl updateARecordSet(String name) {
        return recordSetsImpl.updateARecordSet(name);
    }

    @Override
    public DnsRecordSetImpl updateAaaaRecordSet(String name) {
        return recordSetsImpl.updateAaaaRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl updateMXRecordSet(String name) {
        return recordSetsImpl.updateMXRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl updateNSRecordSet(String name) {
        return recordSetsImpl.updateNSRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl updatePtrRecordSet(String name) {
        return recordSetsImpl.updatePtrRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl updateSrvRecordSet(String name) {
        return recordSetsImpl.updateSrvRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl updateTxtRecordSet(String name) {
        return recordSetsImpl.updateTxtRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl updateSoaRecord() {
        return recordSetsImpl.updateSoaRecordSet();
    }

    @Override
    public DnsZoneImpl withoutARecordSet(String name) {
        recordSetsImpl.withoutARecordSet(name);
        return this;
    }

    @Override
    public DnsZoneImpl withoutAaaaRecordSet(String name) {
        recordSetsImpl.withoutAaaaRecordSet(name);
        return this;
    }

    @Override
    public DnsZoneImpl withoutCNameRecordSet(String name) {
        recordSetsImpl.withoutCNameRecordSet(name);
        return this;
    }

    @Override
    public DnsZoneImpl withoutMXRecordSet(String name) {
        recordSetsImpl.withoutMXRecordSet(name);
        return this;
    }

    @Override
    public DnsZoneImpl withoutNSRecordSet(String name) {
        recordSetsImpl.withoutNSRecordSet(name);
        return this;
    }

    @Override
    public DnsZoneImpl withoutPtrRecordSet(String name) {
        recordSetsImpl.withoutPtrRecordSet(name);
        return this;
    }

    @Override
    public DnsZoneImpl withoutSrvRecordSet(String name) {
        recordSetsImpl.withoutSrvRecordSet(name);
        return this;
    }

    @Override
    public DnsZoneImpl withoutTxtRecordSet(String name) {
        recordSetsImpl.withoutTxtRecordSet(name);
        return this;
    }

    @Override
    public Observable<DnsZone> createResourceAsync() {
        final DnsZoneImpl self = this;
        return this.manager().inner().zones().createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner())
                .map(innerToFluentMap(this))
                .flatMap(new Func1<DnsZone, Observable<? extends DnsZone>>() {
                    @Override
                    public Observable<? extends DnsZone> call(DnsZone dnsZone) {
                        return self.recordSetsImpl.commitAndGetAllAsync()
                            .map(new Func1<List<DnsRecordSetImpl>, DnsZone>() {
                                @Override
                                public DnsZone call(List<DnsRecordSetImpl> recordSets) {
                                    return self;
                                }
                            });
                    }
                });
    }

    @Override
    public Observable<DnsZone> refreshAsync() {
        return super.refreshAsync().map(new Func1<DnsZone, DnsZone>() {
            @Override
            public DnsZone call(DnsZone dnsZone) {
                DnsZoneImpl impl = (DnsZoneImpl) dnsZone;
                impl.initRecordSets();
                return impl;
            }
        });
    }

    @Override
    protected Observable<ZoneInner> getInnerAsync() {
        return this.manager().inner().zones().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    private void initRecordSets() {
        this.aRecordSets = new ARecordSetsImpl(this);
        this.aaaaRecordSets = new AaaaRecordSetsImpl(this);
        this.cnameRecordSets = new CNameRecordSetsImpl(this);
        this.mxRecordSets = new MXRecordSetsImpl(this);
        this.nsRecordSets = new NSRecordSetsImpl(this);
        this.ptrRecordSets = new PtrRecordSetsImpl(this);
        this.srvRecordSets = new SrvRecordSetsImpl(this);
        this.txtRecordSets = new TxtRecordSetsImpl(this);
        this.recordSetsImpl.clearPendingOperations();
    }
}
