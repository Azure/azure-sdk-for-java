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
 * Represents an record set collection associated with a DNS zone.
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
     * @param parent the parent DNS zone of the record set
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

    void withCNameRecordSet(String name, String alias) {
        CNameRecordSetImpl recordSet = CNameRecordSetImpl.newRecordSet(name, this.parent(), this.client);
        recordSet.inner().cnameRecord().withCname(alias);
        setDefaults(prepareDefine(recordSet.withTimeToLive(defaultTtlInSeconds)));
    }

    DnsRecordSetImpl defineMXRecordSet(String name) {
        return setDefaults(prepareDefine(MXRecordSetImpl.newRecordSet(name, this.parent(), this.client)));
    }

    DnsRecordSetImpl defineNSRecordSet(String name) {
        return setDefaults(prepareDefine(NSRecordSetImpl.newRecordSet(name, this.parent(), this.client)));
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

    DnsRecordSetImpl updateMXRecordSet(String name) {
        return prepareUpdate(MXRecordSetImpl.newRecordSet(name, this.parent(), this.client));
    }

    DnsRecordSetImpl updateNSRecordSet(String name) {
        return prepareUpdate(NSRecordSetImpl.newRecordSet(name, this.parent(), this.client));
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

    void withoutCNameRecordSet(String name) {
        prepareRemove(CNameRecordSetImpl.newRecordSet(name, this.parent(), this.client));
    }

    void withoutMXRecordSet(String name) {
        prepareRemove(MXRecordSetImpl.newRecordSet(name, this.parent(), this.client));
    }

    void withoutNSRecordSet(String name) {
        prepareRemove(NSRecordSetImpl.newRecordSet(name, this.parent(), this.client));
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
