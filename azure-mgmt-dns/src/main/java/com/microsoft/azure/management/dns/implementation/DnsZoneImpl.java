package com.microsoft.azure.management.dns.implementation;

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

    DnsZoneImpl(String name,
                final ZoneInner innerModel,
                final ZonesInner innerCollection,
                final RecordSetsInner recordSetsClient,
                final DnsZoneManager trafficManager) {
        super(name, innerModel, trafficManager);
        this.innerCollection = innerCollection;
        this.recordSetsClient = recordSetsClient;
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
    public ARecordSetImpl defineARecordSet(String name) {
        return null;
    }

    @Override
    public AaaaRecordSetImpl defineAaaaRecordSet(String name) {
        return null;
    }

    @Override
    public DnsZoneImpl withCnameRecordSet(String name, String alias) {
        return null;
    }

    @Override
    public MxRecordSetImpl defineMxRecordSet(String name) {
        return null;
    }

    @Override
    public NsRecordSetImpl defineNsRecordSet(String name) {
        return null;
    }

    @Override
    public PtrRecordSetImpl definePtrRecordSet(String name) {
        return null;
    }

    @Override
    public SrvRecordSetImpl defineSrvRecordSet(String name) {
        return null;
    }

    @Override
    public TxtRecordSetImpl defineTxtRecordSet(String name) {
        return null;
    }

    @Override
    public ARecordSetImpl updateARecordSet(String name) {
        return null;
    }

    @Override
    public AaaaRecordSetImpl updateAaaaRecordSet(String name) {
        return null;
    }

    @Override
    public MxRecordSetImpl updateMxRecordSet(String name) {
        return null;
    }

    @Override
    public NsRecordSetImpl updateNsRecordSet(String name) {
        return null;
    }

    @Override
    public PtrRecordSetImpl updatePtrRecordSet(String name) {
        return null;
    }

    @Override
    public SrvRecordSetImpl updateSrvRecordSet(String name) {
        return null;
    }

    @Override
    public TxtRecordSetImpl updateTxtRecordSet(String name) {
        return null;
    }

    @Override
    public DnsZoneImpl withoutARecordSet(String name) {
        return null;
    }

    @Override
    public DnsZoneImpl withoutAaaaRecordSet(String name) {
        return null;
    }

    @Override
    public DnsZoneImpl withoutCnameRecordSet(String name) {
        return null;
    }

    @Override
    public DnsZoneImpl withoutMxRecordSet(String name) {
        return null;
    }

    @Override
    public DnsZoneImpl withoutNsRecordSet(String name) {
        return null;
    }

    @Override
    public DnsZoneImpl withoutPtrRecordSet(String name) {
        return null;
    }

    @Override
    public DnsZoneImpl withoutSrvRecordSet(String name) {
        return null;
    }

    @Override
    public DnsZoneImpl withoutTxtRecordSet(String name) {
        return null;
    }

    @Override
    public Observable<DnsZone> createResourceAsync() {
        return this.innerCollection.createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner())
                .map(innerToFluentMap(this));
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
        this.aaaaRecordSets  = new AaaaRecordSetsImpl(this, this.recordSetsClient);
        this.cnameRecordSets = new CnameRecordSetsImpl(this, this.recordSetsClient);
        this.mxRecordSets = new MxRecordSetsImpl(this, this.recordSetsClient);
        this.nsRecordSets = new NsRecordSetsImpl(this, this.recordSetsClient);
        this.ptrRecordSets = new PtrRecordSetsImpl(this, this.recordSetsClient);
        this.srvRecordSets = new SrvRecordSetsImpl(this, this.recordSetsClient);
        this.txtRecordSets = new TxtRecordSetsImpl(this, this.recordSetsClient);
    }
}
