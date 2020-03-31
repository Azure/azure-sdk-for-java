/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.dns.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.SubResource;
import com.azure.management.dns.models.RecordSetInner;
import com.azure.management.dns.models.ZoneInner;
import com.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.management.resources.fluentcore.utils.PagedConverter;
import com.azure.management.resources.fluentcore.utils.Utils;
import com.azure.management.dns.ARecordSets;
import com.azure.management.dns.AaaaRecordSets;
import com.azure.management.dns.CNameRecordSets;
import com.azure.management.dns.CaaRecordSets;
import com.azure.management.dns.DnsRecordSet;
import com.azure.management.dns.DnsZone;
import com.azure.management.dns.MXRecordSets;
import com.azure.management.dns.NSRecordSets;
import com.azure.management.dns.PtrRecordSets;
import com.azure.management.dns.RecordType;
import com.azure.management.dns.SoaRecordSet;
import com.azure.management.dns.SrvRecordSets;
import com.azure.management.dns.TxtRecordSets;
import com.azure.management.dns.ZoneType;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for {@link DnsZone}.
 */
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
    private CaaRecordSets caaRecordSets;
    private CNameRecordSets cnameRecordSets;
    private MXRecordSets mxRecordSets;
    private NSRecordSets nsRecordSets;
    private PtrRecordSets ptrRecordSets;
    private SrvRecordSets srvRecordSets;
    private TxtRecordSets txtRecordSets;
    private DnsRecordSetsImpl recordSets;
    private String dnsZoneETag;

    DnsZoneImpl(String name, final ZoneInner innerModel, final DnsZoneManager manager) {
        super(name, innerModel, manager);
        this.recordSets = new DnsRecordSetsImpl(this);
        initRecordSets();
        if (isInCreateMode()) {
            // Set the zone type to Public by default
            this.inner().withZoneType(ZoneType.PUBLIC);
        }
    }

    @Override
    public long maxNumberOfRecordSets() {
        return Utils.toPrimitiveLong(this.inner().maxNumberOfRecordSets());
    }

    @Override
    public long numberOfRecordSets() {
        return Utils.toPrimitiveLong(this.inner().numberOfRecordSets());
    }

    @Override
    public String eTag() {
        return this.inner().etag();
    }

    @Override
    public ZoneType accessType() {
        return this.inner().zoneType();
    }

    @Override
    public List<String> registrationVirtualNetworkIds() {
        List<String> list = new ArrayList<>();
        if (this.inner().registrationVirtualNetworks() != null) {
            for (SubResource sb : this.inner().registrationVirtualNetworks()) {
                list.add(sb.getId());
            }
        }
        return list;
    }

    @Override
    public List<String> resolutionVirtualNetworkIds() {
        List<String> list = new ArrayList<>();
        if (this.inner().resolutionVirtualNetworks() != null) {
            for (SubResource sb : this.inner().resolutionVirtualNetworks()) {
                list.add(sb.getId());
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
        if (this.inner() == null) {
            return new ArrayList<>();
        }
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
    public CaaRecordSets caaRecordSets() {
        return this.caaRecordSets;
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
        if (inner == null) {
            return null;
        }
        return new SoaRecordSetImpl(inner.getName(), this, inner);
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
    public DnsZoneImpl withoutARecordSet(String name, String eTag) {
        recordSets.withoutARecordSet(name, eTag);
        return this;
    }

    @Override
    public DnsZoneImpl withoutAaaaRecordSet(String name) {
        return this.withoutAaaaRecordSet(name, null);
    }

    @Override
    public DnsZoneImpl withoutAaaaRecordSet(String name, String eTag) {
        recordSets.withoutAaaaRecordSet(name, eTag);
        return this;
    }

    @Override
    public DnsZoneImpl withoutCaaRecordSet(String name) {
        return this.withoutCaaRecordSet(name, null);
    }

    @Override
    public DnsZoneImpl withoutCaaRecordSet(String name, String eTag) {
        recordSets.withoutCaaRecordSet(name, eTag);
        return this;
    }

    @Override
    public DnsZoneImpl withoutCNameRecordSet(String name) {
        return this.withoutCNameRecordSet(name, null);
    }

    @Override
    public DnsZoneImpl withoutCNameRecordSet(String name, String eTag) {
        recordSets.withoutCNameRecordSet(name, eTag);
        return this;
    }

    @Override
    public DnsZoneImpl withoutMXRecordSet(String name) {
        return this.withoutMXRecordSet(name, null);
    }

    @Override
    public DnsZoneImpl withoutMXRecordSet(String name, String eTag) {
        recordSets.withoutMXRecordSet(name, eTag);
        return this;
    }

    @Override
    public DnsZoneImpl withoutNSRecordSet(String name) {
        return this.withoutNSRecordSet(name, null);
    }

    @Override
    public DnsZoneImpl withoutNSRecordSet(String name, String eTag) {
        recordSets.withoutNSRecordSet(name, eTag);
        return this;
    }

    @Override
    public DnsZoneImpl withoutPtrRecordSet(String name) {
        return this.withoutPtrRecordSet(name, null);
    }

    @Override
    public DnsZoneImpl withoutPtrRecordSet(String name, String eTag) {
        recordSets.withoutPtrRecordSet(name, eTag);
        return this;
    }

    @Override
    public DnsZoneImpl withoutSrvRecordSet(String name) {
        return this.withoutSrvRecordSet(name, null);
    }

    @Override
    public DnsZoneImpl withoutSrvRecordSet(String name, String eTag) {
        recordSets.withoutSrvRecordSet(name, eTag);
        return this;
    }

    @Override
    public DnsZoneImpl withoutTxtRecordSet(String name) {
        return this.withoutTxtRecordSet(name, null);
    }

    @Override
    public DnsZoneImpl withoutTxtRecordSet(String name, String eTag) {
        recordSets.withoutTxtRecordSet(name, eTag);
        return this;
    }

    @Override
    public DnsZoneImpl withETagCheck() {
        if (isInCreateMode()) {
            this.dnsZoneETag = "*";
            return this;
        }
        return this.withETagCheck(this.inner().etag());
    }

    @Override
    public DnsZoneImpl withETagCheck(String eTagValue) {
        this.dnsZoneETag = eTagValue;
        return this;
    }

    @Override
    public Mono<DnsZone> createResourceAsync() {
        return Mono.just(this)
                .flatMap(self -> {
                    if (self.isInCreateMode()) {
                        return self.manager().inner().zones().createOrUpdateAsync(self.resourceGroupName(),
                                self.name(), self.inner(), null/*IfMatch*/, self.dnsZoneETag/*IfNoneMatch*/);
                    } else {
                        return self.manager().inner().zones().createOrUpdateAsync(self.resourceGroupName(),
                                self.name(), self.inner(), self.dnsZoneETag/*IfMatch*/, null/*IfNoneMatch*/);
                    }
                })
                .map(innerToFluentMap(this))
                .map(dnsZone -> {
                    this.dnsZoneETag = null;
                    return dnsZone;
                });
    }

    @Override
    public Mono<Void> afterPostRunAsync(boolean isGroupFaulted) {
        return Mono.just(true)
                .map(ignored -> {
                    recordSets.clear();
                    return ignored;
                })
                .then();
    }

    @Override
    public Mono<DnsZone> refreshAsync() {
        return super.refreshAsync()
                .map(dnsZone -> {
                    DnsZoneImpl impl = (DnsZoneImpl) dnsZone;
                    impl.initRecordSets();
                    return impl;
                });
    }

    @Override
    protected Mono<ZoneInner> getInnerAsync() {
        return this.manager().inner().zones().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    private void initRecordSets() {
        this.aRecordSets = new ARecordSetsImpl(this);
        this.aaaaRecordSets = new AaaaRecordSetsImpl(this);
        this.caaRecordSets = new CaaRecordSetsImpl(this);
        this.cnameRecordSets = new CNameRecordSetsImpl(this);
        this.mxRecordSets = new MXRecordSetsImpl(this);
        this.nsRecordSets = new NSRecordSetsImpl(this);
        this.ptrRecordSets = new PtrRecordSetsImpl(this);
        this.srvRecordSets = new SrvRecordSetsImpl(this);
        this.txtRecordSets = new TxtRecordSetsImpl(this);
        this.recordSets.clear();
    }

    private PagedIterable<DnsRecordSet> listRecordSetsIntern(String recordSetSuffix, Integer pageSize) {
        final DnsZoneImpl self = this;
        PagedFlux<DnsRecordSet> recordSets = PagedConverter.flatMapPage(
                this.manager().inner().recordSets().listByDnsZoneAsync(this.resourceGroupName(), this.name(), pageSize, recordSetSuffix),
                inner -> {
                    DnsRecordSet recordSet = new DnsRecordSetImpl(inner.getName(), inner.getType(), self, inner);
                    switch (recordSet.recordType()) {
                        case A:
                            return Mono.just(new ARecordSetImpl(inner.getName(), self, inner));
                        case AAAA:
                            return Mono.just(new AaaaRecordSetImpl(inner.getName(), self, inner));
                        case CAA:
                            return Mono.just(new CaaRecordSetImpl(inner.getName(), self, inner));
                        case CNAME:
                            return Mono.just(new CNameRecordSetImpl(inner.getName(), self, inner));
                        case MX:
                            return Mono.just(new MXRecordSetImpl(inner.getName(), self, inner));
                        case NS:
                            return Mono.just(new NSRecordSetImpl(inner.getName(), self, inner));
                        case PTR:
                            return Mono.just(new PtrRecordSetImpl(inner.getName(), self, inner));
                        case SOA:
                            return Mono.just(new SoaRecordSetImpl(inner.getName(), self, inner));
                        case SRV:
                            return Mono.just(new SrvRecordSetImpl(inner.getName(), self, inner));
                        case TXT:
                            return Mono.just(new TxtRecordSetImpl(inner.getName(), self, inner));
                        default:
                            return Mono.just(recordSet);
                    }
                });
        return new PagedIterable<>(recordSets);
    }

    @Override
    public DnsZoneImpl withPublicAccess() {
        this.inner().withZoneType(ZoneType.PUBLIC);
        this.inner().withRegistrationVirtualNetworks(null);
        this.inner().withResolutionVirtualNetworks(null);
        return this;
    }

    @Override
    public DnsZoneImpl withPrivateAccess() {
        this.inner().withZoneType(ZoneType.PRIVATE);
        this.inner().withRegistrationVirtualNetworks(null);
        this.inner().withResolutionVirtualNetworks(null);
        return this;
    }

    @Override
    public DnsZoneImpl withPrivateAccess(List<String> registrationVirtualNetworkIds, List<String> resolutionVirtualNetworkIds) {
        this.withPrivateAccess();
        this.inner().withRegistrationVirtualNetworks(new ArrayList<>());
        this.inner().withResolutionVirtualNetworks(new ArrayList<>());
        for (String rvnId : registrationVirtualNetworkIds) {
            SubResource sb = new SubResource();
            sb.setId(rvnId);
            this.inner().registrationVirtualNetworks().add(sb);
        }
        for (String rvnId : resolutionVirtualNetworkIds) {
            SubResource sb = new SubResource();
            sb.setId(rvnId);
            this.inner().resolutionVirtualNetworks().add(sb);
        }
        return this;
    }
}
