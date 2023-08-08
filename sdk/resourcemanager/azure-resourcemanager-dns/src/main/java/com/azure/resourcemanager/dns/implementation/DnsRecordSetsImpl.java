// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.implementation;

import com.azure.resourcemanager.dns.models.DnsRecordSet;
import com.azure.resourcemanager.dns.models.DnsZone;
import com.azure.resourcemanager.dns.fluent.models.RecordSetInner;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesNonCachedImpl;

/** Represents an record set collection associated with a DNS zone. */
class DnsRecordSetsImpl
    extends ExternalChildResourcesNonCachedImpl<DnsRecordSetImpl, DnsRecordSet, RecordSetInner, DnsZoneImpl, DnsZone> {
    /** The default record set ttl in seconds. */
    private static final long DEFAULT_TTL_IN_SECONDS = 3600;

    /**
     * Creates new DnsRecordSetsImpl.
     *
     * @param parent the parent DNS zone of the record set
     */
    DnsRecordSetsImpl(DnsZoneImpl parent) {
        super(parent, parent.taskGroup(), "RecordSet");
    }

    DnsRecordSetImpl defineARecordSet(String name) {
        return setDefaults(prepareInlineDefine(ARecordSetImpl.newRecordSet(name, this.getParent())));
    }

    DnsRecordSetImpl defineAaaaRecordSet(String name) {
        return setDefaults(prepareInlineDefine(AaaaRecordSetImpl.newRecordSet(name, this.getParent())));
    }

    void withCNameRecordSet(String name, String alias) {
        CnameRecordSetImpl recordSet = CnameRecordSetImpl.newRecordSet(name, this.getParent());
        recordSet.innerModel().cnameRecord().withCname(alias);
        setDefaults(prepareInlineDefine(recordSet.withTimeToLive(DEFAULT_TTL_IN_SECONDS)));
    }

    DnsRecordSetImpl defineCaaRecordSet(String name) {
        return setDefaults(prepareInlineDefine(CaaRecordSetImpl.newRecordSet(name, this.getParent())));
    }

    DnsRecordSetImpl defineCNameRecordSet(String name) {
        return setDefaults(prepareInlineDefine(CnameRecordSetImpl.newRecordSet(name, this.getParent())));
    }

    DnsRecordSetImpl defineMXRecordSet(String name) {
        return setDefaults(prepareInlineDefine(MxRecordSetImpl.newRecordSet(name, this.getParent())));
    }

    DnsRecordSetImpl defineNSRecordSet(String name) {
        return setDefaults(prepareInlineDefine(NsRecordSetImpl.newRecordSet(name, this.getParent())));
    }

    DnsRecordSetImpl definePtrRecordSet(String name) {
        return setDefaults(prepareInlineDefine(PtrRecordSetImpl.newRecordSet(name, this.getParent())));
    }

    DnsRecordSetImpl defineSrvRecordSet(String name) {
        return setDefaults(prepareInlineDefine(SrvRecordSetImpl.newRecordSet(name, this.getParent())));
    }

    DnsRecordSetImpl defineTxtRecordSet(String name) {
        return setDefaults(prepareInlineDefine(TxtRecordSetImpl.newRecordSet(name, this.getParent())));
    }

    DnsRecordSetImpl updateARecordSet(String name) {
        return prepareInlineUpdate(ARecordSetImpl.newRecordSet(name, this.getParent()));
    }

    DnsRecordSetImpl updateAaaaRecordSet(String name) {
        return prepareInlineUpdate(AaaaRecordSetImpl.newRecordSet(name, this.getParent()));
    }

    DnsRecordSetImpl updateMXRecordSet(String name) {
        return prepareInlineUpdate(MxRecordSetImpl.newRecordSet(name, this.getParent()));
    }

    DnsRecordSetImpl updateCaaRecordSet(String name) {
        return prepareInlineUpdate(CaaRecordSetImpl.newRecordSet(name, this.getParent()));
    }

    DnsRecordSetImpl updateCNameRecordSet(String name) {
        return prepareInlineUpdate(CnameRecordSetImpl.newRecordSet(name, this.getParent()));
    }

    DnsRecordSetImpl updateNSRecordSet(String name) {
        return prepareInlineUpdate(NsRecordSetImpl.newRecordSet(name, this.getParent()));
    }

    DnsRecordSetImpl updatePtrRecordSet(String name) {
        return prepareInlineUpdate(PtrRecordSetImpl.newRecordSet(name, this.getParent()));
    }

    DnsRecordSetImpl updateSrvRecordSet(String name) {
        return prepareInlineUpdate(SrvRecordSetImpl.newRecordSet(name, this.getParent()));
    }

    DnsRecordSetImpl updateTxtRecordSet(String name) {
        return prepareInlineUpdate(TxtRecordSetImpl.newRecordSet(name, this.getParent()));
    }

    DnsRecordSetImpl updateSoaRecordSet() {
        return prepareInlineUpdate(SoaRecordSetImpl.newRecordSet(this.getParent()));
    }

    void withoutARecordSet(String name, String eTagValue) {
        prepareInlineRemove(ARecordSetImpl.newRecordSet(name, this.getParent()).withETagOnDelete(eTagValue));
    }

    void withoutAaaaRecordSet(String name, String eTagValue) {
        prepareInlineRemove(AaaaRecordSetImpl.newRecordSet(name, this.getParent()).withETagOnDelete(eTagValue));
    }

    void withoutCaaRecordSet(String name, String eTagValue) {
        prepareInlineRemove(CaaRecordSetImpl.newRecordSet(name, this.getParent()).withETagOnDelete(eTagValue));
    }

    void withoutCNameRecordSet(String name, String eTagValue) {
        prepareInlineRemove(CnameRecordSetImpl.newRecordSet(name, this.getParent()).withETagOnDelete(eTagValue));
    }

    void withoutMXRecordSet(String name, String eTagValue) {
        prepareInlineRemove(MxRecordSetImpl.newRecordSet(name, this.getParent()).withETagOnDelete(eTagValue));
    }

    void withoutNSRecordSet(String name, String eTagValue) {
        prepareInlineRemove(NsRecordSetImpl.newRecordSet(name, this.getParent()).withETagOnDelete(eTagValue));
    }

    void withoutPtrRecordSet(String name, String eTagValue) {
        prepareInlineRemove(PtrRecordSetImpl.newRecordSet(name, this.getParent()).withETagOnDelete(eTagValue));
    }

    void withoutSrvRecordSet(String name, String eTagValue) {
        prepareInlineRemove(SrvRecordSetImpl.newRecordSet(name, this.getParent()).withETagOnDelete(eTagValue));
    }

    void withoutTxtRecordSet(String name, String eTagValue) {
        prepareInlineRemove(TxtRecordSetImpl.newRecordSet(name, this.getParent()).withETagOnDelete(eTagValue));
    }

    private DnsRecordSetImpl setDefaults(DnsRecordSetImpl recordSet) {
        return recordSet.withTimeToLive(DEFAULT_TTL_IN_SECONDS);
    }
}
