package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.dns.ARecordSet;
import com.microsoft.azure.management.dns.CnameRecord;
import com.microsoft.azure.management.dns.CnameRecordSet;
import rx.functions.Func1;

/**
 * Implementation of {@link ARecordSet}
 */
class CnameRecordSetImpl
        extends DnsRecordSetImpl<CnameRecordSet, CnameRecordSetImpl>
        implements
            CnameRecordSet,
            CnameRecordSet.Definition,
            CnameRecordSet.Update {
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
        this.refreshInner();
        return this;
    }

    @Override
    public CnameRecordSetImpl withCanonicalName(String canonicalName) {
        this.inner().withCnameRecord(new CnameRecord().withCname(canonicalName));
        return this;
    }

    @Override
    protected Func1<RecordSetInner, CnameRecordSet> innerToFluentMap() {
        return super.innerToFluentMap(this);
    }
}
