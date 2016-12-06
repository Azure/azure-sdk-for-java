/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.dns.DnsRecordSet;
import com.microsoft.azure.management.dns.DnsZone;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesNonCachedImpl;

/**
 * Represents an record set collection associated with a Dns zone.
 */
@LangDefinition
class DnsRecordSetsImpl extends
        ExternalChildResourcesNonCachedImpl<DnsRecordSetImpl,
                                        DnsRecordSet,
                                        RecordSetInner,
                                        DnsZoneImpl,
                                        DnsZone> {
    private final RecordSetsInner client;
    /**
     * The default record set ttl in seconds.
     */
    private final long defaultTtlInSeconds = 3600;

    /**
     * Creates new DnsRecordSetsImpl.
     *
     * @param client the client to perform REST calls on record sets
     * @param parent the parent Dns zone of the record set
     */
    DnsRecordSetsImpl(RecordSetsInner client, DnsZoneImpl parent) {
        super(parent, "RecordSet");
        this.client = client;
    }

    DnsRecordSetImpl defineARecordSet(String name) {
        return setDefaults(prepareDefine(ARecordSetImpl.newRecordSet(name, this.parent(), this.client)));
    }

    DnsRecordSetImpl defineAaaaRecordSet(String name) {
        return setDefaults(prepareDefine(AaaaRecordSetImpl.newRecordSet(name, this.parent(), this.client)));
    }

    void withCnameRecordSet(String name, String alias) {
        CnameRecordSetImpl recordSet = CnameRecordSetImpl.newRecordSet(name, this.parent(), this.client);
        recordSet.inner().cnameRecord().withCname(alias);
        setDefaults(prepareDefine(recordSet.withTimeToLive(defaultTtlInSeconds)));
    }

    DnsRecordSetImpl defineMxRecordSet(String name) {
        return setDefaults(prepareDefine(MxRecordSetImpl.newRecordSet(name, this.parent(), this.client)));
    }

    DnsRecordSetImpl defineNsRecordSet(String name) {
        return setDefaults(prepareDefine(NsRecordSetImpl.newRecordSet(name, this.parent(), this.client)));
    }

    DnsRecordSetImpl definePtrRecordSet(String name) {
        return setDefaults(prepareDefine(PtrRecordSetImpl.newRecordSet(name, this.parent(), this.client)));
    }

    DnsRecordSetImpl defineSrvRecordSet(String name) {
        return setDefaults(prepareDefine(SrvRecordSetImpl.newRecordSet(name, this.parent(), this.client)));
    }

    DnsRecordSetImpl defineTxtRecordSet(String name) {
        return setDefaults(prepareDefine(TxtRecordSetImpl.newRecordSet(name, this.parent(), this.client)));
    }

    DnsRecordSetImpl updateARecordSet(String name) {
        return prepareUpdate(ARecordSetImpl.newRecordSet(name, this.parent(), this.client));
    }

    DnsRecordSetImpl updateAaaaRecordSet(String name) {
        return prepareUpdate(AaaaRecordSetImpl.newRecordSet(name, this.parent(), this.client));
    }

    DnsRecordSetImpl updateMxRecordSet(String name) {
        return prepareUpdate(MxRecordSetImpl.newRecordSet(name, this.parent(), this.client));
    }

    DnsRecordSetImpl updateNsRecordSet(String name) {
        return prepareUpdate(NsRecordSetImpl.newRecordSet(name, this.parent(), this.client));
    }

    DnsRecordSetImpl updatePtrRecordSet(String name) {
        return prepareUpdate(PtrRecordSetImpl.newRecordSet(name, this.parent(), this.client));
    }

    DnsRecordSetImpl updateSrvRecordSet(String name) {
        return prepareUpdate(SrvRecordSetImpl.newRecordSet(name, this.parent(), this.client));
    }

    DnsRecordSetImpl updateTxtRecordSet(String name) {
        return prepareUpdate(TxtRecordSetImpl.newRecordSet(name, this.parent(), this.client));
    }

    DnsRecordSetImpl updateSoaRecordSet() {
        return prepareUpdate(SoaRecordSetImpl.newRecordSet(this.parent(), this.client));
    }

    void withoutARecordSet(String name) {
        prepareRemove(ARecordSetImpl.newRecordSet(name, this.parent(), this.client));
    }

    void withoutAaaaRecordSet(String name) {
        prepareRemove(AaaaRecordSetImpl.newRecordSet(name, this.parent(), this.client));
    }

    void withoutCnameRecordSet(String name) {
        prepareRemove(CnameRecordSetImpl.newRecordSet(name, this.parent(), this.client));
    }

    void withoutMxRecordSet(String name) {
        prepareRemove(MxRecordSetImpl.newRecordSet(name, this.parent(), this.client));
    }

    void withoutNsRecordSet(String name) {
        prepareRemove(NsRecordSetImpl.newRecordSet(name, this.parent(), this.client));
    }

    void withoutPtrRecordSet(String name) {
        prepareRemove(PtrRecordSetImpl.newRecordSet(name, this.parent(), this.client));
    }

    void withoutSrvRecordSet(String name) {
        prepareRemove(SrvRecordSetImpl.newRecordSet(name, this.parent(), this.client));
    }

    void withoutTxtRecordSet(String name) {
        prepareRemove(TxtRecordSetImpl.newRecordSet(name, this.parent(), this.client));
    }

    final void clearPendingOperations() {
        this.childCollection.clear();
    }

    private DnsRecordSetImpl setDefaults(DnsRecordSetImpl recordSet) {
        return recordSet.withTimeToLive(defaultTtlInSeconds);
    }
}
