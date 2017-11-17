/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.google.common.base.Splitter;
import com.microsoft.azure.management.apigeneration.LangDefinition;
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of DnsRecordSet.
 */
@LangDefinition
class DnsRecordSetImpl extends ExternalChildResourceImpl<DnsRecordSet,
            RecordSetInner,
            DnsZoneImpl,
            DnsZone>
        implements DnsRecordSet,
            DnsRecordSet.Definition<DnsZone.DefinitionStages.WithCreate>,
            DnsRecordSet.UpdateDefinition<DnsZone.Update>,
            DnsRecordSet.UpdateCombined {
    protected final RecordSetInner recordSetRemoveInfo;
    private final ETagState eTagState = new ETagState();

    protected DnsRecordSetImpl(final DnsZoneImpl parent, final RecordSetInner innerModel) {
        super(innerModel.name(), parent, innerModel);
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
        String fullyQualifiedType = this.inner().type();
        String[] parts = fullyQualifiedType.split("/");
        return RecordType.fromString(parts[parts.length - 1]);
    }

    @Override
    public long timeToLive() {
        return this.inner().tTL();
    }

    @Override
    public Map<String, String> metadata() {
        if (this.inner().metadata() == null) {
            return Collections.unmodifiableMap(new LinkedHashMap<String, String>());
        }
        return Collections.unmodifiableMap(this.inner().metadata());
    }

    @Override
    public String eTag() {
        return this.inner().etag();
    }

    // Setters

    @Override
    public DnsRecordSetImpl withIPv4Address(String ipv4Address) {
        this.inner()
                .aRecords()
                .add(new ARecord().withIpv4Address(ipv4Address));
        return this;
    }

    @Override
    public DnsRecordSetImpl withoutIPv4Address(String ipv4Address) {
        this.recordSetRemoveInfo
                .aRecords()
                .add(new ARecord().withIpv4Address(ipv4Address));
        return this;
    }

    @Override
    public DnsRecordSetImpl withIPv6Address(String ipv6Address) {
        this.inner()
                .aaaaRecords()
                .add(new AaaaRecord().withIpv6Address(ipv6Address));
        return this;
    }

    @Override
    public DnsRecordSetImpl withoutIPv6Address(String ipv6Address) {
        this.recordSetRemoveInfo
                .aaaaRecords()
                .add(new AaaaRecord().withIpv6Address(ipv6Address));
        return this;
    }

    @Override
    public DnsRecordSetImpl withAlias(String alias) {
        this.inner()
                .cnameRecord()
                .withCname(alias);
        return this;
    }

    @Override
    public DnsRecordSetImpl withMailExchange(String mailExchangeHostName, int priority) {
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
        if (text == null) {
            return this;
        }
        List<String> chunks = new ArrayList<>();
        for (String chunk : Splitter.fixedLength(255).split(text)) {
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
        this.recordSetRemoveInfo
                .txtRecords().add(new TxtRecord().withValue(textChunks));
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
        this.inner().withTTL(ttlInSeconds);
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
        this.recordSetRemoveInfo
                .metadata().put(key, null);
        return this;
    }

    @Override
    public DnsRecordSetImpl withETagCheck() {
        this.eTagState.withImplicitETagCheckOnCreate();
        this.eTagState.withImplicitETagCheckOnUpdate();
        return this;
    }

    @Override
    public DnsRecordSetImpl withETagCheck(String eTagValue) {
        this.eTagState.withExplicitETagCheckOnUpdate(eTagValue);
        return this;
    }

    //

    @Override
    public Observable<DnsRecordSet> createAsync() {
        return createOrUpdateAsync(this.inner());
    }

    @Override
    public Observable<DnsRecordSet> updateAsync() {
        return this.parent().manager().inner().recordSets().getAsync(this.parent().resourceGroupName(),
                this.parent().name(), this.name(), this.recordType())
                .map(new Func1<RecordSetInner, RecordSetInner>() {
                    public RecordSetInner call(RecordSetInner resource) {
                        return prepare(resource);
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
        return this.parent().manager().inner().recordSets().deleteAsync(this.parent().resourceGroupName(),
                this.parent().name(), this.name(), this.recordType(), this.eTagState.ifMatchValueOnDelete());
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
    protected Observable<RecordSetInner> getInnerAsync() {
        return this.parent().manager().inner().recordSets().getAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.name(),
                this.recordType());
    }

    private Observable<DnsRecordSet> createOrUpdateAsync(RecordSetInner resource) {
        final DnsRecordSetImpl self = this;
        return this.parent().manager().inner().recordSets().createOrUpdateAsync(this.parent().resourceGroupName(),
                this.parent().name(), this.name(), this.recordType(), resource, eTagState.ifMatchValueOnUpdate(resource.etag()), eTagState.ifNonMatchValueOnCreate())
                .map(new Func1<RecordSetInner, DnsRecordSet>() {
                    @Override
                    public DnsRecordSet call(RecordSetInner inner) {
                        setInner(inner);
                        self.eTagState.clear();
                        return self;
                    }
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
                resource.withMetadata(new LinkedHashMap<String, String>());
            }
            for (Map.Entry<String, String> keyVal : this.inner().metadata().entrySet()) {
                resource.metadata().put(keyVal.getKey(), keyVal.getValue());
            }
            this.inner().metadata().clear();
        }

        if (this.inner().tTL() != null) {
            resource.withTTL(this.inner().tTL());
            this.inner().withTTL(null);
        }

        return prepareForUpdate(resource);
    }

    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        return resource;
    }

    DnsRecordSetImpl withETagOnDelete(String eTagValue) {
        this.eTagState.withExplicitETagCheckOnDelete(eTagValue);
        return this;
    }

    @LangDefinition
    private class ETagState {
        private boolean doImplicitETagCheckOnCreate;
        private boolean doImplicitETagCheckOnUpdate;
        private String eTagOnUpdate;
        private String eTagOnDelete;

        public ETagState withImplicitETagCheckOnCreate() {
            this.doImplicitETagCheckOnCreate = true;
            return this;
        }

        public ETagState withImplicitETagCheckOnUpdate() {
            this.doImplicitETagCheckOnUpdate = true;
            return this;
        }

        public ETagState withExplicitETagCheckOnUpdate(String eTagValue) {
            this.eTagOnUpdate = eTagValue;
            return this;
        }

        public ETagState withExplicitETagCheckOnDelete(String eTagValue) {
            this.eTagOnDelete = eTagValue;
            return this;
        }


        public ETagState clear() {
            this.doImplicitETagCheckOnCreate = false;
            this.doImplicitETagCheckOnUpdate = false;
            this.eTagOnUpdate = null;
            this.eTagOnDelete = null;
            return this;
        }

        public String ifMatchValueOnUpdate(String currentETagValue) {
            String eTagValue = null;
            if (this.doImplicitETagCheckOnUpdate) {
                eTagValue = currentETagValue;
            }
            if (this.eTagOnUpdate != null) {
                eTagValue = this.eTagOnUpdate;
            }
            return eTagValue;
        }

        public String ifMatchValueOnDelete() {
            return this.eTagOnDelete;
        }

        public String ifNonMatchValueOnCreate() {
            if (this.doImplicitETagCheckOnCreate) {
                return "*";
            }
            return null;
        }
    }
}
