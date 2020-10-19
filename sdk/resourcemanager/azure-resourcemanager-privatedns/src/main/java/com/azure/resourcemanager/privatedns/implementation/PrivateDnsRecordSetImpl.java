// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.implementation;

import com.azure.resourcemanager.privatedns.fluent.models.RecordSetInner;
import com.azure.resourcemanager.privatedns.models.ARecord;
import com.azure.resourcemanager.privatedns.models.AaaaRecord;
import com.azure.resourcemanager.privatedns.models.CnameRecord;
import com.azure.resourcemanager.privatedns.models.MxRecord;
import com.azure.resourcemanager.privatedns.models.PrivateDnsRecordSet;
import com.azure.resourcemanager.privatedns.models.PrivateDnsZone;
import com.azure.resourcemanager.privatedns.models.PtrRecord;
import com.azure.resourcemanager.privatedns.models.RecordType;
import com.azure.resourcemanager.privatedns.models.SrvRecord;
import com.azure.resourcemanager.privatedns.models.TxtRecord;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ETagState;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Implementation of {@link PrivateDnsRecordSet}. */
class PrivateDnsRecordSetImpl
    extends ExternalChildResourceImpl<PrivateDnsRecordSet, RecordSetInner, PrivateDnsZoneImpl, PrivateDnsZone>
    implements PrivateDnsRecordSet,
        PrivateDnsRecordSet.Definition<PrivateDnsZone.DefinitionStages.WithCreate>,
        PrivateDnsRecordSet.UpdateDefinition<PrivateDnsZone.Update>,
        PrivateDnsRecordSet.UpdateCombined {

    protected final RecordSetInner recordSetRemoveInfo;
    protected final String type;
    private final ETagState etagState = new ETagState();

    protected PrivateDnsRecordSetImpl(
        String name, String type, final PrivateDnsZoneImpl parent, final RecordSetInner innerModel) {
        super(name, parent, innerModel);
        this.type = type;
        this.recordSetRemoveInfo = new RecordSetInner()
            .withAaaaRecords(new ArrayList<>())
            .withARecords(new ArrayList<>())
            .withCnameRecord(new CnameRecord())
            .withMxRecords(new ArrayList<>())
            .withPtrRecords(new ArrayList<>())
            .withSrvRecords(new ArrayList<>())
            .withTxtRecords(new ArrayList<>())
            .withMetadata(new HashMap<>());
    }

    @Override
    public String etag() {
        return innerModel().etag();
    }

    @Override
    public Map<String, String> metadata() {
        return innerModel().metadata();
    }

    @Override
    public long timeToLive() {
        return innerModel().ttl() == null ? 0 : innerModel().ttl().longValue();
    }

    @Override
    public String fqdn() {
        return innerModel().fqdn();
    }

    @Override
    public boolean isAutoRegistered() {
        return innerModel().isAutoRegistered();
    }

    @Override
    public PrivateDnsRecordSetImpl withIPv6Address(String ipv6Address) {
        innerModel().aaaaRecords().add(new AaaaRecord().withIpv6Address(ipv6Address));
        return this;
    }

    @Override
    public PrivateDnsRecordSetImpl withoutIPv6Address(String ipv6Address) {
        recordSetRemoveInfo.aaaaRecords().add(new AaaaRecord().withIpv6Address(ipv6Address));
        return this;
    }

    @Override
    public PrivateDnsRecordSetImpl withIPv4Address(String ipv4Address) {
        innerModel().aRecords().add(new ARecord().withIpv4Address(ipv4Address));
        return this;
    }

    @Override
    public PrivateDnsRecordSetImpl withoutIPv4Address(String ipv4Address) {
        recordSetRemoveInfo.aRecords().add(new ARecord().withIpv4Address(ipv4Address));
        return this;
    }

    @Override
    public PrivateDnsRecordSetImpl withAlias(String alias) {
        innerModel().cnameRecord().withCname(alias);
        return this;
    }

    @Override
    public PrivateDnsRecordSetImpl withMailExchange(String mailExchangeHostName, int priority) {
        innerModel().mxRecords().add(
            new MxRecord().withExchange(mailExchangeHostName).withPreference(priority));
        return this;
    }

    @Override
    public PrivateDnsRecordSetImpl withoutMailExchange(String mailExchangeHostName, int priority) {
        recordSetRemoveInfo.mxRecords().add(
            new MxRecord().withExchange(mailExchangeHostName).withPreference(priority));
        return this;
    }

    @Override
    public PrivateDnsRecordSetImpl withTargetDomainName(String targetDomainName) {
        innerModel().ptrRecords().add(new PtrRecord().withPtrdname(targetDomainName));
        return this;
    }

    @Override
    public PrivateDnsRecordSetImpl withoutTargetDomainName(String targetDomainName) {
        recordSetRemoveInfo.ptrRecords().add(new PtrRecord().withPtrdname(targetDomainName));
        return this;
    }

    @Override
    public PrivateDnsRecordSetImpl withAuthoritativeServer(String authoritativeServerHostName) {
        innerModel().soaRecord().withHost(authoritativeServerHostName);
        return this;
    }

    @Override
    public PrivateDnsRecordSetImpl withEmailServer(String emailServerHostName) {
        innerModel().soaRecord().withEmail(emailServerHostName);
        return this;
    }

    @Override
    public PrivateDnsRecordSetImpl withSerialNumber(long serialNumber) {
        innerModel().soaRecord().withSerialNumber(serialNumber);
        return this;
    }

    @Override
    public PrivateDnsRecordSetImpl withRefreshTimeInSeconds(long refreshTimeInSeconds) {
        innerModel().soaRecord().withRefreshTime(refreshTimeInSeconds);
        return this;
    }

    @Override
    public PrivateDnsRecordSetImpl withRetryTimeInSeconds(long retryTimeInSeconds) {
        innerModel().soaRecord().withRetryTime(retryTimeInSeconds);
        return this;
    }

    @Override
    public PrivateDnsRecordSetImpl withExpireTimeInSeconds(long expireTimeInSeconds) {
        innerModel().soaRecord().withExpireTime(expireTimeInSeconds);
        return this;
    }

    @Override
    public PrivateDnsRecordSetImpl withNegativeResponseCachingTimeToLiveInSeconds(long negativeCachingTimeToLive) {
        innerModel().soaRecord().withMinimumTtl(negativeCachingTimeToLive);
        return this;
    }

    @Override
    public PrivateDnsRecordSetImpl withRecord(String target, int port, int priority, int weight) {
        innerModel().srvRecords().add(
            new SrvRecord().withTarget(target).withPort(port).withPriority(priority).withWeight(weight));
        return this;
    }

    @Override
    public PrivateDnsRecordSetImpl withoutRecord(String target, int port, int priority, int weight) {
        recordSetRemoveInfo.srvRecords().add(
            new SrvRecord().withTarget(target).withPort(port).withPriority(priority).withWeight(weight));
        return this;
    }

    @Override
    public PrivateDnsRecordSetImpl withText(String text) {
        if (text == null) {
            return this;
        }
        List<String> chunks = new ArrayList<>();
        for (String chunk : text.split("(?<=\\G.{250})")) {
            chunks.add(chunk);
        }
        innerModel().txtRecords().add(new TxtRecord().withValue(chunks));
        return this;
    }

    @Override
    public PrivateDnsRecordSetImpl withoutText(String text) {
        if (text == null) {
            return this;
        }
        List<String> chunks = new ArrayList<>();
        chunks.add(text);
        return withoutText(chunks);
    }

    @Override
    public PrivateDnsRecordSetImpl withoutText(List<String> textChunks) {
        recordSetRemoveInfo.txtRecords().add(new TxtRecord().withValue(textChunks));
        return this;
    }

    @Override
    public PrivateDnsRecordSetImpl withTimeToLive(long ttlInSeconds) {
        innerModel().withTtl(ttlInSeconds);
        return this;
    }

    @Override
    public PrivateDnsRecordSetImpl withMetadata(String key, String value) {
        if (innerModel().metadata() == null) {
            innerModel().withMetadata(new HashMap<>());
        }
        this.innerModel().metadata().put(key, value);
        return this;
    }

    @Override
    public PrivateDnsRecordSetImpl withoutMetadata(String key) {
        recordSetRemoveInfo.metadata().put(key, null);
        return this;
    }

    @Override
    public PrivateDnsRecordSetImpl withETagCheck() {
        etagState.withImplicitETagCheckOnCreateOrUpdate(isInCreateMode());
        return this;
    }

    @Override
    public PrivateDnsRecordSetImpl withETagCheck(String etagValue) {
        etagState.withExplicitETagCheckOnUpdate(etagValue);
        return this;
    }

    @Override
    public Mono<PrivateDnsRecordSet> createResourceAsync() {
        return createOrUpdateAsync(innerModel());
    }

    @Override
    public Mono<PrivateDnsRecordSet> updateResourceAsync() {
        return parent().manager().serviceClient().getRecordSets()
            .getAsync(parent().resourceGroupName(), parent().name(), recordType(), name())
            .map(recordSetInner -> prepare(recordSetInner))
            .flatMap(recordSetInner -> createOrUpdateAsync(recordSetInner));
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return parent().manager().serviceClient().getRecordSets()
            .deleteAsync(
                parent().resourceGroupName(),
                parent().name(),
                recordType(),
                name(),
                etagState.ifMatchValueOnDelete());
    }

    @Override
    protected Mono<RecordSetInner> getInnerAsync() {
        return parent().manager().serviceClient().getRecordSets()
            .getAsync(parent().resourceGroupName(), parent().name(), recordType(), name());
    }

    @Override
    public String id() {
        return innerModel().id();
    }

    @Override
    public RecordType recordType() {
        String fullyQualifiedType = this.type;
        String[] parts = fullyQualifiedType.split("/");
        return RecordType.fromString(parts[parts.length - 1]);
    }

    @Override
    public PrivateDnsZoneImpl attach() {
        return parent();
    }

    @Override
    public String childResourceKey() {
        return name() + "_" + recordType().toString();
    }

    private Mono<PrivateDnsRecordSet> createOrUpdateAsync(RecordSetInner resource) {
        final PrivateDnsRecordSetImpl self = this;
        return parent().manager().serviceClient().getRecordSets()
            .createOrUpdateAsync(
                parent().resourceGroupName(),
                parent().name(),
                recordType(),
                name(),
                resource,
                etagState.ifMatchValueOnUpdate(resource.etag()),
                etagState.ifNonMatchValueOnCreate())
            .map(recordSetInner -> {
                setInner(recordSetInner);
                self.etagState.clear();
                return self;
            });
    }

    private RecordSetInner prepare(RecordSetInner resource) {
        if (recordSetRemoveInfo.metadata().size() > 0) {
            if (resource.metadata() != null) {
                for (String key : recordSetRemoveInfo.metadata().keySet()) {
                    resource.metadata().remove(key);
                }
            }
            recordSetRemoveInfo.metadata().clear();
        }
        if (innerModel().metadata() != null && innerModel().metadata().size() > 0) {
            if (resource.metadata() == null) {
                resource.withMetadata(new HashMap<>());
            }
            for (Map.Entry<String, String> entry : innerModel().metadata().entrySet()) {
                resource.metadata().put(entry.getKey(), entry.getValue());
            }
            innerModel().metadata().clear();
        }
        if (innerModel().ttl() != null) {
            resource.withTtl(innerModel().ttl());
            this.innerModel().withTtl(null);
        }
        return prepareForUpdate(resource);
    }

    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        return resource;
    }

    PrivateDnsRecordSetImpl withETagOnDelete(String etagValue) {
        etagState.withExplicitETagCheckOnDelete(etagValue);
        return this;
    }

    private boolean isInCreateMode() {
        return innerModel().id() == null;
    }
}
