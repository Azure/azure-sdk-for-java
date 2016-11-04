package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.dns.CnameRecordSet;

/**
 * Implementation of {@link CnameRecordSet}.
 */
class CnameRecordSetImpl
        extends DnsRecordSetImpl
        implements CnameRecordSet {
    CnameRecordSetImpl(final DnsZoneImpl parentDnsZone, final RecordSetInner innerModel, final RecordSetsInner client) {
        super(parentDnsZone, innerModel, client);
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
