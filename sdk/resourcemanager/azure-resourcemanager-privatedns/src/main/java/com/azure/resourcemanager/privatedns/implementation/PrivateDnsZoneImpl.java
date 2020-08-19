// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.privatedns.PrivateDnsZoneManager;
import com.azure.resourcemanager.privatedns.fluent.inner.PrivateZoneInner;
import com.azure.resourcemanager.privatedns.fluent.inner.RecordSetInner;
import com.azure.resourcemanager.privatedns.models.ARecordSets;
import com.azure.resourcemanager.privatedns.models.AaaaRecordSets;
import com.azure.resourcemanager.privatedns.models.CnameRecordSets;
import com.azure.resourcemanager.privatedns.models.MxRecordSets;
import com.azure.resourcemanager.privatedns.models.PrivateDnsRecordSet;
import com.azure.resourcemanager.privatedns.models.PrivateDnsZone;
import com.azure.resourcemanager.privatedns.models.ProvisioningState;
import com.azure.resourcemanager.privatedns.models.PtrRecordSets;
import com.azure.resourcemanager.privatedns.models.RecordType;
import com.azure.resourcemanager.privatedns.models.SoaRecordSet;
import com.azure.resourcemanager.privatedns.models.SrvRecordSets;
import com.azure.resourcemanager.privatedns.models.TxtRecordSets;
import com.azure.resourcemanager.privatedns.models.VirtualNetworkLinks;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ETagState;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import reactor.core.publisher.Mono;

/** Implementation for {@link PrivateDnsZone}. */
class PrivateDnsZoneImpl
    extends GroupableResourceImpl<PrivateDnsZone, PrivateZoneInner, PrivateDnsZoneImpl, PrivateDnsZoneManager>
    implements PrivateDnsZone, PrivateDnsZone.Definition, PrivateDnsZone.Update {

    private AaaaRecordSets aaaaRecordSets;
    private ARecordSets aRecordSets;
    private CnameRecordSets cnameRecordSets;
    private MxRecordSets mxRecordSets;
    private PtrRecordSets ptrRecordSets;
    private SrvRecordSets srvRecordSets;
    private TxtRecordSets txtRecordSets;
    private PrivateDnsRecordSetsImpl recordSets;
    private VirtualNetworkLinksImpl virtualNetworkLinks;
    private final ETagState etagState = new ETagState();

    PrivateDnsZoneImpl(String name, final PrivateZoneInner innerModel, final PrivateDnsZoneManager manager) {
        super(name, innerModel, manager);
        this.recordSets = new PrivateDnsRecordSetsImpl(this);
        this.virtualNetworkLinks = new VirtualNetworkLinksImpl(this);
        initRecordSets();
    }

    private void initRecordSets() {
        aaaaRecordSets = new AaaaRecordSetsImpl(this);
        aRecordSets = new ARecordSetsImpl(this);
        cnameRecordSets = new CnameRecordSetsImpl(this);
        mxRecordSets = new MxRecordSetsImpl(this);
        ptrRecordSets = new PtrRecordSetsImpl(this);
        srvRecordSets = new SrvRecordSetsImpl(this);
        txtRecordSets = new TxtRecordSetsImpl(this);
    }

    @Override
    public String etag() {
        return inner().etag();
    }

    @Override
    public long maxNumberOfRecordSets() {
        return Utils.toPrimitiveLong(inner().maxNumberOfRecordSets());
    }

    @Override
    public long numberOfRecordSets() {
        return Utils.toPrimitiveLong(inner().numberOfRecordSets());
    }

    @Override
    public long maxNumberOfVirtualNetworkLinks() {
        return Utils.toPrimitiveLong(inner().maxNumberOfVirtualNetworkLinks());
    }

    @Override
    public long numberOfVirtualNetworkLinks() {
        return Utils.toPrimitiveLong(inner().numberOfVirtualNetworkLinks());
    }

    @Override
    public long maxNumberOfVirtualNetworkLinksWithRegistration() {
        return Utils.toPrimitiveLong(inner().maxNumberOfVirtualNetworkLinksWithRegistration());
    }

    @Override
    public long numberOfVirtualNetworkLinksWithRegistration() {
        return Utils.toPrimitiveLong(inner().numberOfVirtualNetworkLinksWithRegistration());
    }

    @Override
    public ProvisioningState provisioningState() {
        return inner().provisioningState();
    }

    @Override
    public PagedIterable<PrivateDnsRecordSet> listRecordSets() {
        return listRecordSetsIntern(null, null);
    }

    @Override
    public PagedFlux<PrivateDnsRecordSet> listRecordSetsAsync() {
        return listRecordSetsInternAsync(null, null);
    }

    @Override
    public PagedIterable<PrivateDnsRecordSet> listRecordSets(String recordSetNameSuffix) {
        return listRecordSetsIntern(recordSetNameSuffix, null);
    }

    @Override
    public PagedFlux<PrivateDnsRecordSet> listRecordSetsAsync(String recordSetNameSuffix) {
        return listRecordSetsInternAsync(recordSetNameSuffix, null);
    }

    @Override
    public PagedIterable<PrivateDnsRecordSet> listRecordSets(int pageSize) {
        return listRecordSetsIntern(null, pageSize);
    }

    @Override
    public PagedFlux<PrivateDnsRecordSet> listRecordSetsAsync(int pageSize) {
        return listRecordSetsInternAsync(null, pageSize);
    }

    @Override
    public PagedIterable<PrivateDnsRecordSet> listRecordSets(String recordSetNameSuffix, int pageSize) {
        return listRecordSetsIntern(recordSetNameSuffix, pageSize);
    }

    @Override
    public PagedFlux<PrivateDnsRecordSet> listRecordSetsAsync(String recordSetNameSuffix, int pageSize) {
        return listRecordSetsInternAsync(recordSetNameSuffix, pageSize);
    }

    @Override
    public AaaaRecordSets aaaaRecordSets() {
        return aaaaRecordSets;
    }

    @Override
    public ARecordSets aRecordSets() {
        return aRecordSets;
    }

    @Override
    public CnameRecordSets cnameRecordSets() {
        return cnameRecordSets;
    }

    @Override
    public MxRecordSets mxRecordSets() {
        return mxRecordSets;
    }

    @Override
    public PtrRecordSets ptrRecordSets() {
        return ptrRecordSets;
    }

    @Override
    public SoaRecordSet getSoaRecordSet() {
        RecordSetInner inner = manager().inner().getRecordSets().get(resourceGroupName(), name(), RecordType.SOA, "@");
        return inner == null ? null : new SoaRecordSetImpl(inner.name(), this, inner);
    }

    @Override
    public SrvRecordSets srvRecordSets() {
        return srvRecordSets;
    }

    @Override
    public TxtRecordSets txtRecordSets() {
        return txtRecordSets;
    }

    @Override
    public VirtualNetworkLinks virtualNetworkLinks() {
        return virtualNetworkLinks;
    }

    @Override
    public PrivateDnsRecordSetImpl defineAaaaRecordSet(String name) {
        return recordSets.defineAaaaRecordSet(name);
    }

    @Override
    public PrivateDnsRecordSetImpl defineARecordSet(String name) {
        return recordSets.defineARecordSet(name);
    }

    @Override
    public PrivateDnsZoneImpl withCnameRecordSet(String name, String alias) {
        recordSets.withCnameRecordSet(name, alias);
        return this;
    }

    @Override
    public PrivateDnsRecordSetImpl defineCnameRecordSet(String name) {
        return recordSets.defineCnameRecordSet(name);
    }

    @Override
    public PrivateDnsRecordSetImpl defineMxRecordSet(String name) {
        return recordSets.defineMxRecordSet(name);
    }

    @Override
    public PrivateDnsRecordSetImpl definePtrRecordSet(String name) {
        return recordSets.definePtrRecordSet(name);
    }

    @Override
    public PrivateDnsRecordSetImpl defineSoaRecordSet() {
        return recordSets.defineSoaRecordSet();
    }

    @Override
    public PrivateDnsRecordSetImpl defineSrvRecordSet(String name) {
        return recordSets.defineSrvRecordSet(name);
    }

    @Override
    public PrivateDnsRecordSetImpl defineTxtRecordSet(String name) {
        return recordSets.defineTxtRecordSet(name);
    }

    @Override
    public PrivateDnsRecordSetImpl updateAaaaRecordSet(String name) {
        return recordSets.updateAaaaRecordSet(name);
    }

    @Override
    public PrivateDnsRecordSetImpl updateARecordSet(String name) {
        return recordSets.updateARecordSet(name);
    }

    @Override
    public PrivateDnsRecordSetImpl updateCnameRecordSet(String name) {
        return recordSets.updateCnameRecordSet(name);
    }

    @Override
    public PrivateDnsRecordSetImpl updateMxRecordSet(String name) {
        return recordSets.updateMxRecordSet(name);
    }

    @Override
    public PrivateDnsRecordSetImpl updatePtrRecordSet(String name) {
        return recordSets.updatePtrRecordSet(name);
    }

    @Override
    public PrivateDnsRecordSetImpl updateSoaRecord() {
        return recordSets.updateSoaRecordSet();
    }

    @Override
    public PrivateDnsRecordSetImpl updateSrvRecordSet(String name) {
        return recordSets.updateSrvRecordSet(name);
    }

    @Override
    public PrivateDnsRecordSetImpl updateTxtRecordSet(String name) {
        return recordSets.updateTxtRecordSet(name);
    }

    @Override
    public PrivateDnsZoneImpl withoutAaaaRecordSet(String name) {
        return withoutAaaaRecordSet(name, null);
    }

    @Override
    public PrivateDnsZoneImpl withoutAaaaRecordSet(String name, String etagValue) {
        recordSets.withoutAaaaRecordSet(name, etagValue);
        return this;
    }

    @Override
    public PrivateDnsZoneImpl withoutARecordSet(String name) {
        return withoutARecordSet(name, null);
    }

    @Override
    public PrivateDnsZoneImpl withoutARecordSet(String name, String etagValue) {
        recordSets.withoutARecordSet(name, etagValue);
        return this;
    }

    @Override
    public PrivateDnsZoneImpl withoutCNameRecordSet(String name) {
        return withoutCNameRecordSet(name, null);
    }

    @Override
    public PrivateDnsZoneImpl withoutCNameRecordSet(String name, String etagValue) {
        recordSets.withoutCnameRecordSet(name, etagValue);
        return this;
    }

    @Override
    public PrivateDnsZoneImpl withoutMXRecordSet(String name) {
        return withoutMXRecordSet(name, null);
    }

    @Override
    public PrivateDnsZoneImpl withoutMXRecordSet(String name, String etagValue) {
        recordSets.withoutMxRecordSet(name, etagValue);
        return this;
    }

    @Override
    public PrivateDnsZoneImpl withoutPtrRecordSet(String name) {
        return withoutPtrRecordSet(name, null);
    }

    @Override
    public PrivateDnsZoneImpl withoutPtrRecordSet(String name, String etagValue) {
        recordSets.withoutPtrRecordSet(name, etagValue);
        return this;
    }

    @Override
    public PrivateDnsZoneImpl withoutSrvRecordSet(String name) {
        return withoutSrvRecordSet(name, null);
    }

    @Override
    public PrivateDnsZoneImpl withoutSrvRecordSet(String name, String etagValue) {
        recordSets.withoutSrvRecordSet(name, etagValue);
        return this;
    }

    @Override
    public PrivateDnsZoneImpl withoutTxtRecordSet(String name) {
        return withoutTxtRecordSet(name, null);
    }

    @Override
    public PrivateDnsZoneImpl withoutTxtRecordSet(String name, String etagValue) {
        recordSets.withoutTxtRecordSet(name, etagValue);
        return this;
    }

    @Override
    public VirtualNetworkLinkImpl defineVirtualNetworkLink(String name) {
        return virtualNetworkLinks.defineVirtualNetworkLink(name);
    }

    @Override
    public VirtualNetworkLinkImpl updateVirtualNetworkLink(String name) {
        return virtualNetworkLinks.updateVirtualNetworkLink(name);
    }

    @Override
    public PrivateDnsZoneImpl withoutVirtualNetworkLink(String name) {
        return withoutVirtualNetworkLink(name, null);
    }

    @Override
    public PrivateDnsZoneImpl withoutVirtualNetworkLink(String name, String etagValue) {
        virtualNetworkLinks.withoutVirtualNetworkLink(name, etagValue);
        return this;
    }

    @Override
    public PrivateDnsZoneImpl withETagCheck() {
        etagState.withImplicitETagCheckOnCreateOrUpdate(isInCreateMode());
        return this;
    }

    @Override
    public Update withETagCheck(String etagValue) {
        etagState.withExplicitETagCheckOnUpdate(etagValue);
        return this;
    }

    @Override
    public Mono<PrivateDnsZone> createResourceAsync() {
        return manager().inner().getPrivateZones()
            .createOrUpdateAsync(
                resourceGroupName(),
                name(),
                inner(),
                etagState.ifMatchValueOnUpdate(inner().etag()),
                etagState.ifNonMatchValueOnCreate())
            .map(innerToFluentMap(this))
            .map(privateDnsZone -> {
                etagState.clear();
                return privateDnsZone;
            });
    }

    @Override
    public Mono<Void> afterPostRunAsync(boolean isGroupFaulted) {
        return Mono.just(true).map(ignored -> {
            recordSets.clear();
            virtualNetworkLinks.clear();
            return ignored;
        }).then();
    }

    @Override
    public Mono<PrivateDnsZone> refreshAsync() {
        return super.refreshAsync().map(privateDnsZone -> {
            PrivateDnsZoneImpl impl = (PrivateDnsZoneImpl) privateDnsZone;
            impl.initRecordSets();
            return impl;
        });
    }

    @Override
    protected Mono<PrivateZoneInner> getInnerAsync() {
        return manager().inner().getPrivateZones().getByResourceGroupAsync(resourceGroupName(), name());
    }

    private PagedIterable<PrivateDnsRecordSet> listRecordSetsIntern(String recordSetSuffix, Integer pageSize) {
        return new PagedIterable<>(listRecordSetsInternAsync(recordSetSuffix, pageSize));
    }

    private PagedFlux<PrivateDnsRecordSet> listRecordSetsInternAsync(String recordSetSuffix, Integer pageSize) {
        final PrivateDnsZoneImpl self = this;
        return manager().inner().getRecordSets().listAsync(resourceGroupName(), name(), pageSize, recordSetSuffix)
            .mapPage(recordSetInner -> {
                PrivateDnsRecordSet recordSet = new PrivateDnsRecordSetImpl(
                    recordSetInner.name(), recordSetInner.type(), self, recordSetInner);
                switch (recordSet.recordType()) {
                    case AAAA:
                        return new AaaaRecordSetImpl(recordSetInner.name(), self, recordSetInner);
                    case A:
                        return new ARecordSetImpl(recordSetInner.name(), self, recordSetInner);
                    case CNAME:
                        return new CnameRecordSetImpl(recordSetInner.name(), self, recordSetInner);
                    case MX:
                        return new MxRecordSetImpl(recordSetInner.name(), self, recordSetInner);
                    case PTR:
                        return new PtrRecordSetImpl(recordSetInner.name(), self, recordSetInner);
                    case SOA:
                        return new SoaRecordSetImpl(recordSetInner.name(), self, recordSetInner);
                    case SRV:
                        return new SrvRecordSetImpl(recordSetInner.name(), self, recordSetInner);
                    case TXT:
                        return new TxtRecordSetImpl(recordSetInner.name(), self, recordSetInner);
                    default:
                        return recordSet;
                }
            });
    }
}
