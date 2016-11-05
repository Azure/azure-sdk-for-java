package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.dns.RecordType;
import com.microsoft.azure.management.dns.SoaRecord;
import com.microsoft.azure.management.dns.SoaRecordSet;

/**
 * Implementation of {@link SoaRecordSet}.
 */
class SoaRecordSetImpl
        extends DnsRecordSetImpl
        implements SoaRecordSet {
    SoaRecordSetImpl(final DnsZoneImpl parentDnsZone, final RecordSetInner innerModel, final RecordSetsInner client) {
        super(parentDnsZone, innerModel, client);
    }

    static SoaRecordSetImpl newRecordSet(final DnsZoneImpl parentDnsZone, final RecordSetsInner client) {
        return new SoaRecordSetImpl(parentDnsZone,
                new RecordSetInner()
                        .withName("@")
                        .withType(RecordType.SOA.toString())
                        .withSoaRecord(new SoaRecord()),
                    client);
    }

    @Override
    public SoaRecord record() {
        return this.inner().soaRecord();
    }

    @Override
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        if (resource.soaRecord() == null) {
            resource.withSoaRecord(new SoaRecord());
        }

        if (this.inner().soaRecord().email() != null) {
            resource.soaRecord().withEmail(this.inner().soaRecord().email());
        }

        if (this.inner().soaRecord().expireTime() != null) {
            resource.soaRecord().withExpireTime(this.inner().soaRecord().expireTime());
        }

        if (this.inner().soaRecord().minimumTtl() != null) {
            resource.soaRecord().withMinimumTtl(this.inner().soaRecord().minimumTtl());
        }

        if (this.inner().soaRecord().refreshTime() != null) {
            resource.soaRecord().withRefreshTime(this.inner().soaRecord().refreshTime());
        }

        if (this.inner().soaRecord().retryTime() != null) {
            resource.soaRecord().withRefreshTime(this.inner().soaRecord().retryTime());
        }

        if (this.inner().soaRecord().serialNumber() != null) {
            resource.soaRecord().withSerialNumber(this.inner().soaRecord().serialNumber());
        }

        this.inner().withSoaRecord(new SoaRecord());
        return resource;
    }
}
