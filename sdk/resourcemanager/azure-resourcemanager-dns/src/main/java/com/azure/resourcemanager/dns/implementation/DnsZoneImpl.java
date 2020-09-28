// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.SubResource;
import com.azure.resourcemanager.dns.DnsZoneManager;
import com.azure.resourcemanager.dns.models.ARecordSets;
import com.azure.resourcemanager.dns.models.AaaaRecordSets;
import com.azure.resourcemanager.dns.models.CnameRecordSets;
import com.azure.resourcemanager.dns.models.CaaRecordSets;
import com.azure.resourcemanager.dns.models.DnsRecordSet;
import com.azure.resourcemanager.dns.models.DnsZone;
import com.azure.resourcemanager.dns.models.MxRecordSets;
import com.azure.resourcemanager.dns.models.NsRecordSets;
import com.azure.resourcemanager.dns.models.PtrRecordSets;
import com.azure.resourcemanager.dns.models.RecordType;
import com.azure.resourcemanager.dns.models.SoaRecordSet;
import com.azure.resourcemanager.dns.models.SrvRecordSets;
import com.azure.resourcemanager.dns.models.TxtRecordSets;
import com.azure.resourcemanager.dns.models.ZoneType;
import com.azure.resourcemanager.dns.fluent.models.RecordSetInner;
import com.azure.resourcemanager.dns.fluent.models.ZoneInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ETagState;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/** Implementation for {@link DnsZone}. */
public class DnsZoneImpl extends GroupableResourceImpl<DnsZone, ZoneInner, DnsZoneImpl, DnsZoneManager>
    implements DnsZone, DnsZone.Definition, DnsZone.Update {

    private ARecordSets aRecordSets;
    private AaaaRecordSets aaaaRecordSets;
    private CaaRecordSets caaRecordSets;
    private CnameRecordSets cnameRecordSets;
    private MxRecordSets mxRecordSets;
    private NsRecordSets nsRecordSets;
    private PtrRecordSets ptrRecordSets;
    private SrvRecordSets srvRecordSets;
    private TxtRecordSets txtRecordSets;
    private DnsRecordSetsImpl recordSets;
    private final ETagState etagState = new ETagState();

    DnsZoneImpl(String name, final ZoneInner innerModel, final DnsZoneManager manager) {
        super(name, innerModel, manager);
        this.recordSets = new DnsRecordSetsImpl(this);
        initRecordSets();
        if (isInCreateMode()) {
            // Set the zone type to Public by default
            this.innerModel().withZoneType(ZoneType.PUBLIC);
        }
    }

    @Override
    public long maxNumberOfRecordSets() {
        return ResourceManagerUtils.toPrimitiveLong(this.innerModel().maxNumberOfRecordSets());
    }

    @Override
    public long numberOfRecordSets() {
        return ResourceManagerUtils.toPrimitiveLong(this.innerModel().numberOfRecordSets());
    }

    @Override
    public String etag() {
        return this.innerModel().etag();
    }

    @Override
    public ZoneType accessType() {
        return this.innerModel().zoneType();
    }

    @Override
    public List<String> registrationVirtualNetworkIds() {
        List<String> list = new ArrayList<>();
        if (this.innerModel().registrationVirtualNetworks() != null) {
            for (SubResource sb : this.innerModel().registrationVirtualNetworks()) {
                list.add(sb.id());
            }
        }
        return list;
    }

    @Override
    public List<String> resolutionVirtualNetworkIds() {
        List<String> list = new ArrayList<>();
        if (this.innerModel().resolutionVirtualNetworks() != null) {
            for (SubResource sb : this.innerModel().resolutionVirtualNetworks()) {
                list.add(sb.id());
            }
        }
        return list;
    }

    @Override
    public PagedIterable<DnsRecordSet> listRecordSets() {
        return this.listRecordSetsIntern(null, null);
    }

    @Override
    public PagedIterable<DnsRecordSet> listRecordSets(String recordSetNameSuffix) {
        return this.listRecordSetsIntern(recordSetNameSuffix, null);
    }

    @Override
    public PagedIterable<DnsRecordSet> listRecordSets(int pageSize) {
        return this.listRecordSetsIntern(null, pageSize);
    }

    @Override
    public PagedIterable<DnsRecordSet> listRecordSets(String recordSetNameSuffix, int pageSize) {
        return this.listRecordSetsIntern(recordSetNameSuffix, pageSize);
    }

    @Override
    public List<String> nameServers() {
        if (this.innerModel() == null) {
            return new ArrayList<>();
        }
        return this.innerModel().nameServers();
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
    public CaaRecordSets caaRecordSets() {
        return this.caaRecordSets;
    }

    @Override
    public CnameRecordSets cNameRecordSets() {
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
        RecordSetInner inner = this.manager().serviceClient().getRecordSets()
            .get(this.resourceGroupName(), this.name(), "@", RecordType.SOA);
        if (inner == null) {
            return null;
        }
        return new SoaRecordSetImpl(inner.name(), this, inner);
    }

    // Setters

    @Override
    public DnsRecordSetImpl defineARecordSet(String name) {
        return recordSets.defineARecordSet(name);
    }

    @Override
    public DnsRecordSetImpl defineAaaaRecordSet(String name) {
        return recordSets.defineAaaaRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl defineCaaRecordSet(String name) {
        return recordSets.defineCaaRecordSet(name);
    }

    @Override
    public DnsZoneImpl withCNameRecordSet(String name, String alias) {
        recordSets.withCNameRecordSet(name, alias);
        return this;
    }

    @Override
    public DnsRecordSetImpl defineCNameRecordSet(String name) {
        return recordSets.defineCNameRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl defineMXRecordSet(String name) {
        return recordSets.defineMXRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl defineNSRecordSet(String name) {
        return recordSets.defineNSRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl definePtrRecordSet(String name) {
        return recordSets.definePtrRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl defineSrvRecordSet(String name) {
        return recordSets.defineSrvRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl defineTxtRecordSet(String name) {
        return recordSets.defineTxtRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl updateARecordSet(String name) {
        return recordSets.updateARecordSet(name);
    }

    @Override
    public DnsRecordSetImpl updateAaaaRecordSet(String name) {
        return recordSets.updateAaaaRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl updateCaaRecordSet(String name) {
        return recordSets.updateCaaRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl updateMXRecordSet(String name) {
        return recordSets.updateMXRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl updateCNameRecordSet(String name) {
        return recordSets.updateCNameRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl updateNSRecordSet(String name) {
        return recordSets.updateNSRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl updatePtrRecordSet(String name) {
        return recordSets.updatePtrRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl updateSrvRecordSet(String name) {
        return recordSets.updateSrvRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl updateTxtRecordSet(String name) {
        return recordSets.updateTxtRecordSet(name);
    }

    @Override
    public DnsRecordSetImpl updateSoaRecord() {
        return recordSets.updateSoaRecordSet();
    }

    @Override
    public DnsZoneImpl withoutARecordSet(String name) {
        return this.withoutARecordSet(name, null);
    }

    @Override
    public DnsZoneImpl withoutARecordSet(String name, String etag) {
        recordSets.withoutARecordSet(name, etag);
        return this;
    }

    @Override
    public DnsZoneImpl withoutAaaaRecordSet(String name) {
        return this.withoutAaaaRecordSet(name, null);
    }

    @Override
    public DnsZoneImpl withoutAaaaRecordSet(String name, String etag) {
        recordSets.withoutAaaaRecordSet(name, etag);
        return this;
    }

    @Override
    public DnsZoneImpl withoutCaaRecordSet(String name) {
        return this.withoutCaaRecordSet(name, null);
    }

    @Override
    public DnsZoneImpl withoutCaaRecordSet(String name, String etag) {
        recordSets.withoutCaaRecordSet(name, etag);
        return this;
    }

    @Override
    public DnsZoneImpl withoutCNameRecordSet(String name) {
        return this.withoutCNameRecordSet(name, null);
    }

    @Override
    public DnsZoneImpl withoutCNameRecordSet(String name, String etag) {
        recordSets.withoutCNameRecordSet(name, etag);
        return this;
    }

    @Override
    public DnsZoneImpl withoutMXRecordSet(String name) {
        return this.withoutMXRecordSet(name, null);
    }

    @Override
    public DnsZoneImpl withoutMXRecordSet(String name, String etag) {
        recordSets.withoutMXRecordSet(name, etag);
        return this;
    }

    @Override
    public DnsZoneImpl withoutNSRecordSet(String name) {
        return this.withoutNSRecordSet(name, null);
    }

    @Override
    public DnsZoneImpl withoutNSRecordSet(String name, String etag) {
        recordSets.withoutNSRecordSet(name, etag);
        return this;
    }

    @Override
    public DnsZoneImpl withoutPtrRecordSet(String name) {
        return this.withoutPtrRecordSet(name, null);
    }

    @Override
    public DnsZoneImpl withoutPtrRecordSet(String name, String etag) {
        recordSets.withoutPtrRecordSet(name, etag);
        return this;
    }

    @Override
    public DnsZoneImpl withoutSrvRecordSet(String name) {
        return this.withoutSrvRecordSet(name, null);
    }

    @Override
    public DnsZoneImpl withoutSrvRecordSet(String name, String etag) {
        recordSets.withoutSrvRecordSet(name, etag);
        return this;
    }

    @Override
    public DnsZoneImpl withoutTxtRecordSet(String name) {
        return this.withoutTxtRecordSet(name, null);
    }

    @Override
    public DnsZoneImpl withoutTxtRecordSet(String name, String etag) {
        recordSets.withoutTxtRecordSet(name, etag);
        return this;
    }

    @Override
    public DnsZoneImpl withETagCheck() {
        this.etagState.withImplicitETagCheckOnCreateOrUpdate(this.isInCreateMode());
        return this;
    }

    @Override
    public DnsZoneImpl withETagCheck(String etagValue) {
        this.etagState.withExplicitETagCheckOnUpdate(etagValue);
        return this;
    }

    @Override
    public Mono<DnsZone> createResourceAsync() {
        return Mono
            .just(this)
            .flatMap(
                self ->
                    self
                        .manager()
                        .serviceClient()
                        .getZones()
                        .createOrUpdateAsync(
                            self.resourceGroupName(),
                            self.name(),
                            self.innerModel(),
                            etagState.ifMatchValueOnUpdate(self.innerModel().etag()),
                            etagState.ifNonMatchValueOnCreate()))
            .map(innerToFluentMap(this))
            .map(
                dnsZone -> {
                    this.etagState.clear();
                    return dnsZone;
                });
    }

    @Override
    public Mono<Void> afterPostRunAsync(boolean isGroupFaulted) {
        return Mono
            .just(true)
            .map(
                ignored -> {
                    recordSets.clear();
                    return ignored;
                })
            .then();
    }

    @Override
    public Mono<DnsZone> refreshAsync() {
        return super
            .refreshAsync()
            .map(
                dnsZone -> {
                    DnsZoneImpl impl = (DnsZoneImpl) dnsZone;
                    impl.initRecordSets();
                    return impl;
                });
    }

    @Override
    protected Mono<ZoneInner> getInnerAsync() {
        return this.manager().serviceClient().getZones().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    private void initRecordSets() {
        this.aRecordSets = new ARecordSetsImpl(this);
        this.aaaaRecordSets = new AaaaRecordSetsImpl(this);
        this.caaRecordSets = new CaaRecordSetsImpl(this);
        this.cnameRecordSets = new CnameRecordSetsImpl(this);
        this.mxRecordSets = new MxRecordSetsImpl(this);
        this.nsRecordSets = new NsRecordSetsImpl(this);
        this.ptrRecordSets = new PtrRecordSetsImpl(this);
        this.srvRecordSets = new SrvRecordSetsImpl(this);
        this.txtRecordSets = new TxtRecordSetsImpl(this);
        this.recordSets.clear();
    }

    private PagedIterable<DnsRecordSet> listRecordSetsIntern(String recordSetSuffix, Integer pageSize) {
        final DnsZoneImpl self = this;
        PagedFlux<DnsRecordSet> recordSets =
            PagedConverter
                .flatMapPage(
                    this
                        .manager()
                        .serviceClient()
                        .getRecordSets()
                        .listByDnsZoneAsync(this.resourceGroupName(), this.name(), pageSize, recordSetSuffix),
                    inner -> {
                        DnsRecordSet recordSet = new DnsRecordSetImpl(inner.name(), inner.type(), self, inner);
                        switch (recordSet.recordType()) {
                            case A:
                                return Mono.just(new ARecordSetImpl(inner.name(), self, inner));
                            case AAAA:
                                return Mono.just(new AaaaRecordSetImpl(inner.name(), self, inner));
                            case CAA:
                                return Mono.just(new CaaRecordSetImpl(inner.name(), self, inner));
                            case CNAME:
                                return Mono.just(new CnameRecordSetImpl(inner.name(), self, inner));
                            case MX:
                                return Mono.just(new MxRecordSetImpl(inner.name(), self, inner));
                            case NS:
                                return Mono.just(new NsRecordSetImpl(inner.name(), self, inner));
                            case PTR:
                                return Mono.just(new PtrRecordSetImpl(inner.name(), self, inner));
                            case SOA:
                                return Mono.just(new SoaRecordSetImpl(inner.name(), self, inner));
                            case SRV:
                                return Mono.just(new SrvRecordSetImpl(inner.name(), self, inner));
                            case TXT:
                                return Mono.just(new TxtRecordSetImpl(inner.name(), self, inner));
                            default:
                                return Mono.just(recordSet);
                        }
                    });
        return new PagedIterable<>(recordSets);
    }
}
