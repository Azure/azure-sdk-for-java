package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.dns.ARecordSet;
import com.microsoft.azure.management.dns.CnameRecordSet;
import rx.functions.Func1;

/**
 * Implementation of {@link ARecordSet}
 */
class CnameRecordSetImpl
        extends DnsRecordSetImpl<CnameRecordSet, CnameRecordSetImpl>
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
    public CnameRecordSetImpl refresh() {
        this.resetInner();
        return this;
    }

    @Override
    protected Func1<RecordSetInner, CnameRecordSet> innerToFluentMap() {
        return super.innerToFluentMap(this);
    }
}
