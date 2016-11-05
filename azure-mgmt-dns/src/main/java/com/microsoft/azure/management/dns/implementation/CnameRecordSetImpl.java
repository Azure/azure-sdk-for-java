package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.dns.CnameRecord;
import com.microsoft.azure.management.dns.CnameRecordSet;
import com.microsoft.azure.management.dns.RecordType;

/**
 * Implementation of {@link CnameRecordSet}.
 */
class CnameRecordSetImpl
        extends DnsRecordSetImpl
        implements CnameRecordSet {
    CnameRecordSetImpl(final DnsZoneImpl parentDnsZone, final RecordSetInner innerModel, final RecordSetsInner client) {
        super(parentDnsZone, innerModel, client);
    }

    static CnameRecordSetImpl newRecordSet(final String name, final DnsZoneImpl parentDnsZone, final RecordSetsInner client) {
        return new CnameRecordSetImpl(parentDnsZone,
                new RecordSetInner()
                        .withName(name)
                        .withType(RecordType.CNAME.toString())
                        .withCnameRecord(new CnameRecord()),
                client);
    }

    @Override
    public String canonicalName() {
        if ( this.inner().cnameRecord() != null) {
            return this.inner().cnameRecord().cname();
        }
        return null;
    }

    @Override
    protected RecordSetInner prepareForUpdate(RecordSetInner resource) {
        return resource;
    }
}
