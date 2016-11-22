/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.dns.ARecordSets;
import com.microsoft.azure.management.dns.AaaaRecordSets;
import com.microsoft.azure.management.dns.CnameRecordSets;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.MxRecordSets;
import com.microsoft.azure.management.dns.NsRecordSets;
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
    private final ZonesInner innerCollection;
    private final RecordSetsInner recordSetsClient;

    private ARecordSets aRecordSets;
    private AaaaRecordSets aaaaRecordSets;
    private CnameRecordSets cnameRecordSets;
    private MxRecordSets mxRecordSets;
    private NsRecordSets nsRecordSets;
    private PtrRecordSets ptrRecordSets;
    private SrvRecordSets srvRecordSets;
    private TxtRecordSets txtRecordSets;
    private DnsRecordSetsImpl recordSetsImpl;

    DnsZoneImpl(String name,
                final ZoneInner innerModel,
                final ZonesInner innerCollection,
                final RecordSetsInner recordSetsClient,
                final DnsZoneManager trafficManager) {
        super(name, innerModel, trafficManager);
        this.innerCollection = innerCollection;
        this.recordSetsClient = recordSetsClient;
        this.recordSetsImpl = new DnsRecordSetsImpl(recordSetsClient, this);
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
    public CnameRecordSets cnameRecordSets() {
        return this.cnameRecordSets;
    }

    @Override
    public MxRecordSets mxRecordSets() {
        return this.mxRecordSets;
    }

    @Override
    public NsRecordSets nsRecordSets() {
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
        RecordSetInner inner = this.recordSetsClient.get(this.resourceGroupName(), this.name(), "@", RecordType.SOA);
        return new SoaRecordSetImpl(this, inner, this.recordSetsClient);
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
    public DnsZoneImpl withCnameRecordSet(String name, String alias) {
        recordSetsImpl.withCnameRecordSet(name, alias);
        return this;
    }

    @Override
    public DnsRecordSetImpl defineMxRecordSet(String name) {
        return recordSetsImpl.defineMxRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl defineNsRecordSet(String name) {
        return recordSetsImpl.defineNsRecordSet(name);
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
    public DnsRecordSetImpl updateMxRecordSet(String name) {
        return recordSetsImpl.updateMxRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl updateNsRecordSet(String name) {
        return recordSetsImpl.updateNsRecordSet(name);
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
    public DnsZoneImpl withoutCnameRecordSet(String name) {
        recordSetsImpl.withoutCnameRecordSet(name);
        return this;
    }

    @Override
    public DnsZoneImpl withoutMxRecordSet(String name) {
        recordSetsImpl.withoutMxRecordSet(name);
        return this;
    }

    @Override
    public DnsZoneImpl withoutNsRecordSet(String name) {
        recordSetsImpl.withoutNsRecordSet(name);
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
        return this.innerCollection.createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner())
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
    public DnsZone refresh() {
        ZoneInner inner = this.innerCollection.get(this.resourceGroupName(), this.name());
        this.setInner(inner);
        this.initRecordSets();
        return this;
    }

    private void initRecordSets() {
        this.aRecordSets = new ARecordSetsImpl(this, this.recordSetsClient);
        this.aaaaRecordSets = new AaaaRecordSetsImpl(this, this.recordSetsClient);
        this.cnameRecordSets = new CnameRecordSetsImpl(this, this.recordSetsClient);
        this.mxRecordSets = new MxRecordSetsImpl(this, this.recordSetsClient);
        this.nsRecordSets = new NsRecordSetsImpl(this, this.recordSetsClient);
        this.ptrRecordSets = new PtrRecordSetsImpl(this, this.recordSetsClient);
        this.srvRecordSets = new SrvRecordSetsImpl(this, this.recordSetsClient);
        this.txtRecordSets = new TxtRecordSetsImpl(this, this.recordSetsClient);
        this.recordSetsImpl.clearPendingOperations();
    }
}
