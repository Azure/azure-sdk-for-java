package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.dns.DnsRecordSet;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import rx.Observable;
import rx.functions.Func1;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of {@link DnsRecordSet}.
 *
 * @param <FluentModelT> The record set fluent model type
 * @param <FluentModelImplT> the implementation type of the fluent model type
 */
abstract class DnsRecordSetImpl<FluentModelT,
            FluentModelImplT extends DnsRecordSetImpl<FluentModelT, FluentModelImplT>>
        extends CreatableUpdatableImpl<FluentModelT, RecordSetInner, FluentModelImplT>
        implements DnsRecordSet<FluentModelT, DnsZone> {
    protected final DnsZoneImpl dnsZone;
    protected final RecordSetsInner client;

    protected DnsRecordSetImpl(final DnsZoneImpl parentDnsZone, final RecordSetInner innerModel, final RecordSetsInner client) {
        super(innerModel.name(), innerModel);
        this.dnsZone = parentDnsZone;
        this.client = client;
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

    @Override
    public DnsZoneImpl parent() {
        return this.dnsZone;
    }

    @Override
    public Map<String, String> tags() {
        if (this.inner().metadata() == null) {
            return Collections.unmodifiableMap(new LinkedHashMap<String, String>());
        }
        return Collections.unmodifiableMap(this.inner().metadata());
    }

    public final FluentModelImplT withTags(Map<String, String> tags) {
        this.inner().withMetadata(new HashMap<>(tags));
        return (FluentModelImplT) this;
    }

    public final FluentModelImplT withTag(String key, String value) {
        this.inner().metadata().put(key, value);
        return (FluentModelImplT) this;
    }

    public final FluentModelImplT withoutTag(String key) {
        this.inner().metadata().remove(key);
        return (FluentModelImplT) this;
    }

    public FluentModelImplT withTimeToLive(long ttlInSeconds) {
        this.inner().withTTL(ttlInSeconds);
        return (FluentModelImplT) this;
    }

    @Override
    public boolean isInCreateMode() {
        return this.inner().id() == null;
    }

    @Override
    public Observable<FluentModelT> createResourceAsync() {
        return client.createOrUpdateAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.name(),
                this.recordType(),
                this.inner())
                .map(innerToFluentMap());
    }

    protected void refreshInner() {
        this.setInner(this.client.get(this.parent().resourceGroupName(),
                this.parent().name(),
                this.name(),
                this.recordType()));
    }

    protected abstract Func1<RecordSetInner, FluentModelT> innerToFluentMap();
}
