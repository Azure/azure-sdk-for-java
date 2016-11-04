package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.dns.ARecord;
import com.microsoft.azure.management.dns.AaaaRecord;
import com.microsoft.azure.management.dns.CnameRecord;
import com.microsoft.azure.management.dns.DnsRecordSet;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.MxRecord;
import com.microsoft.azure.management.dns.NsRecord;
import com.microsoft.azure.management.dns.PtrRecord;
import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.dns.SrvRecord;
import com.microsoft.azure.management.dns.TxtRecord;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link DnsRecordSet}.
 */
abstract class DnsRecordSetImpl extends ExternalChildResourceImpl<DnsRecordSet,
        RecordSetInner,
        DnsZoneImpl,
        DnsZone>
        implements DnsRecordSet,
        DnsRecordSet.Definition<DnsZone.DefinitionStages.WithCreate>,
        DnsRecordSet.UpdateDefinition<DnsZone.Update>,
        DnsRecordSet.UpdateCombined {
    protected final RecordSetsInner client;
    private RecordSetInner recordSetRemoveInfo;

    protected DnsRecordSetImpl(final DnsZoneImpl parent, final RecordSetInner innerModel, final RecordSetsInner client) {
        super(innerModel.name(), parent, innerModel);
        this.client = client;
        this.recordSetRemoveInfo = new RecordSetInner()
            .withName(innerModel.name())
            .withType(innerModel.type())
            .withARecords(new ArrayList<ARecord>())
            .withAaaaRecords(new ArrayList<AaaaRecord>())
            .withCnameRecord(new CnameRecord())
            .withMxRecords(new ArrayList<MxRecord>())
            .withNsRecords(new ArrayList<NsRecord>())
            .withPtrRecords(new ArrayList<PtrRecord>())
            .withSrvRecords(new ArrayList<SrvRecord>())
            .withTxtRecords(new ArrayList<TxtRecord>())
            .withMetadata(new LinkedHashMap<String, String>());
    }

    @Override
    public String id() {
      return inner().id();
    }

    @Override
    public RecordType recordType() {
        return RecordType.fromString(this.inner().type());
    }

    @Override
    public long timeToLive() {
        return this.inner().tTL();
    }

    // Setters

    @Override
    public DnsRecordSetImpl withIpv4Address(String ipv4Address) {
        if (this.inner().aRecords() == null) {
            this.inner().withARecords(new ArrayList<ARecord>());
        }
        this.inner()
                .aRecords()
                .add(new ARecord().withIpv4Address(ipv4Address));
        return this;
    }

    @Override
    public DnsRecordSetImpl withoutIpv4Address(String ipv4Address) {
        this.recordSetRemoveInfo
                .aRecords()
                .add(new ARecord().withIpv4Address(ipv4Address));
        return this;
    }

    @Override
    public DnsRecordSetImpl withIpv6Address(String ipv6Address) {
        if (this.inner().aaaaRecords() == null) {
            this.inner().withAaaaRecords(new ArrayList<AaaaRecord>());
        }
        this.inner()
                .aaaaRecords()
                .add(new AaaaRecord().withIpv6Address(ipv6Address));
        return this;
    }

    @Override
    public DnsRecordSetImpl withoutIpv6Address(String ipv6Address) {
        this.recordSetRemoveInfo
                .aaaaRecords()
                .add(new AaaaRecord().withIpv6Address(ipv6Address));
        return this;
    }

    @Override
    public DnsRecordSetImpl withMailExchange(String mailExchangeHostName, int priority) {
        if (this.inner().mxRecords() == null) {
            this.inner().withMxRecords(new ArrayList<MxRecord>());
        }
        this.inner()
                .mxRecords()
                .add(new MxRecord().withExchange(mailExchangeHostName).withPreference(priority));
        return this;
    }

    @Override
    public DnsRecordSetImpl withoutMailExchange(String mailExchangeHostName, int priority) {
        this.recordSetRemoveInfo
                .mxRecords()
                .add(new MxRecord().withExchange(mailExchangeHostName).withPreference(priority));
        return this;
    }

    @Override
    public DnsRecordSetImpl withNameServer(String nameServerHostName) {
        if (this.inner().nsRecords() == null) {
            this.inner().withNsRecords(new ArrayList<NsRecord>());
        }
        this.inner()
                .nsRecords()
                .add(new NsRecord().withNsdname(nameServerHostName));
        return this;
    }

    @Override
    public DnsRecordSetImpl withoutNameServer(String nameServerHostName) {
        this.recordSetRemoveInfo
                .nsRecords()
                .add(new NsRecord().withNsdname(nameServerHostName));
        return this;
    }

    @Override
    public DnsRecordSetImpl withTargetDomainName(String targetDomainName) {
        if (this.inner().ptrRecords() == null) {
            this.inner().withPtrRecords(new ArrayList<PtrRecord>());
        }
        this.inner()
                .ptrRecords()
                .add(new PtrRecord().withPtrdname(targetDomainName));
        return this;
    }

    @Override
    public DnsRecordSetImpl withoutTargetDomainName(String targetDomainName) {
        this.recordSetRemoveInfo
                .ptrRecords()
                .add(new PtrRecord().withPtrdname(targetDomainName));
        return this;
    }

    @Override
    public DnsRecordSetImpl withRecord(String target, int port, int priority, int weight) {
        if (this.inner().srvRecords() == null) {
            this.inner().withSrvRecords(new ArrayList<SrvRecord>());
        }
        this.inner().srvRecords().add(new SrvRecord()
                .withTarget(target)
                .withPort(port)
                .withPriority(priority)
                .withWeight(weight));
        return this;
    }

    @Override
    public DnsRecordSetImpl withoutRecord(String target, int port, int priority, int weight) {
        this.recordSetRemoveInfo.
                srvRecords().add(new SrvRecord()
                    .withTarget(target)
                    .withPort(port)
                    .withPriority(priority)
                    .withWeight(weight));
        return this;
    }

    @Override
    public DnsRecordSetImpl withText(String text) {
        if (this.inner().txtRecords() == null) {
            this.inner().withTxtRecords(new ArrayList<TxtRecord>());
        }
        List<String> value = new ArrayList<>();
        value.add(text);
        this.inner().txtRecords().add(new TxtRecord().withValue(value));
        return this;
    }

    @Override
    public DnsRecordSetImpl withoutText(String text) {
        List<String> value = new ArrayList<>();
        value.add(text);
        this.recordSetRemoveInfo
                .txtRecords().add(new TxtRecord().withValue(value));
        return this;
    }

    @Override
    public DnsRecordSetImpl withTimeToLive(long ttlInSeconds) {
        this.inner().withTTL(ttlInSeconds);
        return this;
    }

    @Override
    public DnsRecordSetImpl withTags(Map<String, String> tags) {
        this.inner().withMetadata(new HashMap<>(tags));
        return this;
    }

    @Override
    public DnsRecordSetImpl withTag(String key, String value) {
        this.inner().metadata().put(key, value);
        return this;
    }

    @Override
    public DnsRecordSetImpl withoutTag(String key) {
        this.recordSetRemoveInfo
                .metadata().put(key, null);
        return this;
    }

    //

    @Override
    public Observable<DnsRecordSet> createAsync() {
        return createOrUpdateAsync(this.inner());
    }

    @Override
    public Observable<DnsRecordSet> updateAsync() {
        return this.client.getAsync(this.parent().resourceGroupName(),
                this.parent().name(), this.name(), this.recordType())
                .map(new Func1<RecordSetInner, RecordSetInner>() {
                    public RecordSetInner call(RecordSetInner resource) {
                        return merge(resource, recordSetRemoveInfo);
                    }
                }).flatMap(new Func1<RecordSetInner, Observable<DnsRecordSet>>() {
                    @Override
                    public Observable<DnsRecordSet> call(RecordSetInner resource) {
                        return createOrUpdateAsync(resource);
                    }
                });
    }

    @Override
    public Observable<Void> deleteAsync() {
        return this.client.deleteAsync(this.parent().resourceGroupName(),
                this.parent().name(), this.name(), this.recordType());
    }

    @Override
    public DnsZoneImpl attach() {
        // TODO add record
        return this.parent();
    }

    @Override
    public DnsRecordSetImpl refresh() {
        this.setInner(this.client.get(this.parent().resourceGroupName(),
                this.parent().name(),
                this.name(),
                this.recordType()));
        return this;
    }

    private Observable<DnsRecordSet> createOrUpdateAsync(RecordSetInner resource) {
        final DnsRecordSetImpl self = this;
        return this.client.createOrUpdateAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.name(),
                this.recordType(),
                resource)
                .map(new Func1<RecordSetInner, DnsRecordSet>() {
                    @Override
                    public DnsRecordSet call(RecordSetInner inner) {
                        self.setInner(inner);
                        return self;
                    }
                });
    }

    protected abstract RecordSetInner merge(RecordSetInner resource, RecordSetInner recordSetRemoveInfo);
}
