// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.implementation;

import com.azure.resourcemanager.dns.models.ARecord;
import com.azure.resourcemanager.dns.models.AaaaRecord;
import com.azure.resourcemanager.dns.models.CaaRecord;
import com.azure.resourcemanager.dns.models.CnameRecord;
import com.azure.resourcemanager.dns.models.DnsRecordSet;
import com.azure.resourcemanager.dns.models.DnsZone;
import com.azure.resourcemanager.dns.models.MxRecord;
import com.azure.resourcemanager.dns.models.NsRecord;
import com.azure.resourcemanager.dns.models.PtrRecord;
import com.azure.resourcemanager.dns.models.RecordType;
import com.azure.resourcemanager.dns.models.SrvRecord;
import com.azure.resourcemanager.dns.models.TxtRecord;
import com.azure.resourcemanager.dns.fluent.inner.RecordSetInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ETagState;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Implementation of DnsRecordSet. */
class DnsRecordSetImpl extends ExternalChildResourceImpl<DnsRecordSet, RecordSetInner, DnsZoneImpl, DnsZone>
    implements DnsRecordSet,
        DnsRecordSet.Definition<DnsZone.DefinitionStages.WithCreate>,
        DnsRecordSet.UpdateDefinition<DnsZone.Update>,
        DnsRecordSet.UpdateCombined {
    protected final RecordSetInner recordSetRemoveInfo;
    protected final String type;
    private final ETagState etagState = new ETagState();

    protected DnsRecordSetImpl(String name, String type, final DnsZoneImpl parent, final RecordSetInner innerModel) {
        super(name, parent, innerModel);
        this.type = type;
        this.recordSetRemoveInfo =
            new RecordSetInner()
                .withARecords(new ArrayList<>())
                .withAaaaRecords(new ArrayList<>())
                .withCaaRecords(new ArrayList<>())
                .withCnameRecord(new CnameRecord())
                .withMxRecords(new ArrayList<>())
                .withNsRecords(new ArrayList<>())
                .withPtrRecords(new ArrayList<>())
                .withSrvRecords(new ArrayList<>())
                .withTxtRecords(new ArrayList<>())
                .withMetadata(new LinkedHashMap<>());
    }

    @Override
    public String id() {
        return inner().id();
    }

    @Override
    public RecordType recordType() {
        String fullyQualifiedType = this.type;
        String[] parts = fullyQualifiedType.split("/");
        return RecordType.fromString(parts[parts.length - 1]);
    }

    @Override
    public long timeToLive() {
        return this.inner().ttl();
    }

    @Override
    public Map<String, String> metadata() {
        if (this.inner().metadata() == null) {
            return Collections.unmodifiableMap(new LinkedHashMap<String, String>());
        }
        return Collections.unmodifiableMap(this.inner().metadata());
    }

    @Override
    public String fqdn() {
        return this.inner().fqdn();
    }

    @Override
    public String etag() {
        return this.inner().etag();
    }

    // Setters

    @Override
    public DnsRecordSetImpl withIPv4Address(String ipv4Address) {
        this.inner().aRecords().add(new ARecord().withIpv4Address(ipv4Address));
        return this;
    }

    @Override
    public DnsRecordSetImpl withoutIPv4Address(String ipv4Address) {
        this.recordSetRemoveInfo.aRecords().add(new ARecord().withIpv4Address(ipv4Address));
        return this;
    }

    @Override
    public DnsRecordSetImpl withIPv6Address(String ipv6Address) {
        this.inner().aaaaRecords().add(new AaaaRecord().withIpv6Address(ipv6Address));
        return this;
    }

    @Override
    public DnsRecordSetImpl withoutIPv6Address(String ipv6Address) {
        this.recordSetRemoveInfo.aaaaRecords().add(new AaaaRecord().withIpv6Address(ipv6Address));
        return this;
    }

    @Override
    public DnsRecordSetImpl withAlias(String alias) {
        this.inner().cnameRecord().withCname(alias);
        return this;
    }

    @Override
    public DnsRecordSetImpl withMailExchange(String mailExchangeHostName, int priority) {
        this.inner().mxRecords().add(new MxRecord().withExchange(mailExchangeHostName).withPreference(priority));
        return this;
    }

    @Override
    public DnsRecordSetImpl withoutMailExchange(String mailExchangeHostName, int priority) {
        this
            .recordSetRemoveInfo
            .mxRecords()
            .add(new MxRecord().withExchange(mailExchangeHostName).withPreference(priority));
        return this;
    }

    @Override
    public DnsRecordSetImpl withNameServer(String nameServerHostName) {
        this.inner().nsRecords().add(new NsRecord().withNsdname(nameServerHostName));
        return this;
    }

    @Override
    public DnsRecordSetImpl withoutNameServer(String nameServerHostName) {
        this.recordSetRemoveInfo.nsRecords().add(new NsRecord().withNsdname(nameServerHostName));
        return this;
    }

    @Override
    public DnsRecordSetImpl withTargetDomainName(String targetDomainName) {
        this.inner().ptrRecords().add(new PtrRecord().withPtrdname(targetDomainName));
        return this;
    }

    @Override
    public DnsRecordSetImpl withoutTargetDomainName(String targetDomainName) {
        this.recordSetRemoveInfo.ptrRecords().add(new PtrRecord().withPtrdname(targetDomainName));
        return this;
    }

    @Override
    public DnsRecordSetImpl withRecord(int flags, String tag, String value) {
        this.inner().caaRecords().add(new CaaRecord().withFlags(flags).withTag(tag).withValue(value));
        return this;
    }

    @Override
    public DnsRecordSetImpl withoutRecord(int flags, String tag, String value) {
        this.recordSetRemoveInfo.caaRecords().add(new CaaRecord().withFlags(flags).withTag(tag).withValue(value));
        return this;
    }

    @Override
    public DnsRecordSetImpl withRecord(String target, int port, int priority, int weight) {
        this
            .inner()
            .srvRecords()
            .add(new SrvRecord().withTarget(target).withPort(port).withPriority(priority).withWeight(weight));
        return this;
    }

    @Override
    public DnsRecordSetImpl withoutRecord(String target, int port, int priority, int weight) {
        this
            .recordSetRemoveInfo
            .srvRecords()
            .add(new SrvRecord().withTarget(target).withPort(port).withPriority(priority).withWeight(weight));
        return this;
    }

    @Override
    public DnsRecordSetImpl withText(String text) {
        if (text == null) {
            return this;
        }
        List<String> chunks = new ArrayList<>();
        for (String chunk : text.split("(?<=\\G.{250})")) {
            chunks.add(chunk);
        }
        this.inner().txtRecords().add(new TxtRecord().withValue(chunks));
        return this;
    }

    @Override
    public DnsRecordSetImpl withoutText(String text) {
        if (text == null) {
            return this;
        }
        List<String> chunks = new ArrayList<>();
        chunks.add(text);
        return withoutText(chunks);
    }

    @Override
    public DnsRecordSetImpl withoutText(List<String> textChunks) {
        this.recordSetRemoveInfo.txtRecords().add(new TxtRecord().withValue(textChunks));
        return this;
    }

    @Override
    public DnsRecordSetImpl withEmailServer(String emailServerHostName) {
        this.inner().soaRecord().withEmail(emailServerHostName);
        return this;
    }

    @Override
    public DnsRecordSetImpl withRefreshTimeInSeconds(long refreshTimeInSeconds) {
        this.inner().soaRecord().withRefreshTime(refreshTimeInSeconds);
        return this;
    }

    @Override
    public DnsRecordSetImpl withRetryTimeInSeconds(long retryTimeInSeconds) {
        this.inner().soaRecord().withRetryTime(retryTimeInSeconds);
        return this;
    }

    @Override
    public DnsRecordSetImpl withExpireTimeInSeconds(long expireTimeInSeconds) {
        this.inner().soaRecord().withExpireTime(expireTimeInSeconds);
        return this;
    }

    @Override
    public DnsRecordSetImpl withNegativeResponseCachingTimeToLiveInSeconds(long negativeCachingTimeToLive) {
        this.inner().soaRecord().withMinimumTtl(negativeCachingTimeToLive);
        return this;
    }

    @Override
    public DnsRecordSetImpl withSerialNumber(long serialNumber) {
        this.inner().soaRecord().withSerialNumber(serialNumber);
        return this;
    }

    @Override
    public DnsRecordSetImpl withTimeToLive(long ttlInSeconds) {
        this.inner().withTtl(ttlInSeconds);
        return this;
    }

    @Override
    public DnsRecordSetImpl withMetadata(String key, String value) {
        if (this.inner().metadata() == null) {
            this.inner().withMetadata(new LinkedHashMap<String, String>());
        }
        this.inner().metadata().put(key, value);
        return this;
    }

    @Override
    public DnsRecordSetImpl withoutMetadata(String key) {
        this.recordSetRemoveInfo.metadata().put(key, null);
        return this;
    }

    @Override
    public DnsRecordSetImpl withETagCheck() {
        this.etagState.withImplicitETagCheckOnCreateOrUpdate(this.isInCreateMode());
        return this;
    }

    @Override
    public DnsRecordSetImpl withETagCheck(String etagValue) {
        this.etagState.withExplicitETagCheckOnUpdate(etagValue);
        return this;
    }

    //

    @Override
    public Mono<DnsRecordSet> createResourceAsync() {
        return createOrUpdateAsync(this.inner());
    }

    @Override
    public Mono<DnsRecordSet> updateResourceAsync() {
        return this
            .parent()
            .manager()
            .inner()
            .getRecordSets()
            .getAsync(this.parent().resourceGroupName(), this.parent().name(), this.name(), this.recordType())
            .map(recordSetInner -> prepare(recordSetInner))
            .flatMap(recordSetInner -> createOrUpdateAsync(recordSetInner));
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return this
            .parent()
            .manager()
            .inner()
            .getRecordSets()
            .deleteAsync(
                this.parent().resourceGroupName(),
                this.parent().name(),
                this.name(),
                this.recordType(),
                this.etagState.ifMatchValueOnDelete());
    }

    @Override
    public DnsZoneImpl attach() {
        return this.parent();
    }

    @Override
    public String childResourceKey() {
        return this.name() + "_" + this.recordType().toString();
    }

    @Override
    protected Mono<RecordSetInner> getInnerAsync() {
        return this
            .parent()
            .manager()
            .inner()
            .getRecordSets()
            .getAsync(this.parent().resourceGroupName(), this.parent().name(), this.name(), this.recordType());
    }

    private Mono<DnsRecordSet> createOrUpdateAsync(RecordSetInner resource) {
        final DnsRecordSetImpl self = this;
        return this
            .parent()
            .manager()
            .inner()
            .getRecordSets()
            .createOrUpdateAsync(
                this.parent().resourceGroupName(),
                this.parent().name(),
                this.name(),
                this.recordType(),
                resource,
                etagState.ifMatchValueOnUpdate(resource.etag()),
                etagState.ifNonMatchValueOnCreate())
            .map(
                recordSetInner -> {
                    setInner(recordSetInner);
                    self.etagState.clear();
                    return self;
                });
    }

    private RecordSetInner prepare(RecordSetInner resource) {
        if (this.recordSetRemoveInfo.metadata().size() > 0) {
            if (resource.metadata() != null) {
                for (String key : this.recordSetRemoveInfo.metadata().keySet()) {
                    resource.metadata().remove(key);
                }
            }
            this.recordSetRemoveInfo.metadata().clear();
        }
        if (this.inner().metadata() != null && this.inner().metadata().size() > 0) {
            if (resource.metadata() == null) {
                resource.withMetadata(new LinkedHashMap<>());
            }
            for (Map.Entry<String, String> keyVal : this.inner().metadata().entrySet()) {
                resource.metadata().put(keyVal.getKey(), keyVal.getValue());
            }
            this.inner().metadata().clear();
        }

        if (this.inner().ttl() != null) {
            resource.withTtl(this.inner().ttl());
            this.inner().withTtl(null);
        }

        return prepareForUpdate(resource);
    }

    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        return resource;
    }

    DnsRecordSetImpl withETagOnDelete(String etagValue) {
        this.etagState.withExplicitETagCheckOnDelete(etagValue);
        return this;
    }

    private boolean isInCreateMode() {
        return this.inner().id() == null;
    }
}
