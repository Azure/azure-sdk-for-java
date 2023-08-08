// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.implementation;

import com.azure.resourcemanager.privatedns.fluent.models.RecordSetInner;
import com.azure.resourcemanager.privatedns.models.PrivateDnsRecordSet;
import com.azure.resourcemanager.privatedns.models.PrivateDnsZone;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesNonCachedImpl;

/** Represents an record set collection associated with a private DNS zone. */
class PrivateDnsRecordSetsImpl
    extends ExternalChildResourcesNonCachedImpl<
        PrivateDnsRecordSetImpl,
        PrivateDnsRecordSet,
        RecordSetInner,
        PrivateDnsZoneImpl,
        PrivateDnsZone> {
    /** The default record set ttl in seconds. */
    private static final long DEFAULT_TTL_IN_SECONDS = 3600;

    PrivateDnsRecordSetsImpl(PrivateDnsZoneImpl parent) {
        super(parent, parent.taskGroup(), "PrivateRecordSet");
    }

    private PrivateDnsRecordSetImpl setDefaults(PrivateDnsRecordSetImpl recordSet) {
        return recordSet.withTimeToLive(DEFAULT_TTL_IN_SECONDS);
    }

    PrivateDnsRecordSetImpl defineAaaaRecordSet(String name) {
        return setDefaults(prepareInlineDefine(AaaaRecordSetImpl.newRecordSet(name, getParent())));
    }

    PrivateDnsRecordSetImpl defineARecordSet(String name) {
        return setDefaults(prepareInlineDefine(ARecordSetImpl.newRecordSet(name, getParent())));
    }

    void withCnameRecordSet(String name, String alias) {
        CnameRecordSetImpl recordSet = CnameRecordSetImpl.newRecordSet(name, getParent());
        recordSet.innerModel().cnameRecord().withCname(alias);
        setDefaults(prepareInlineDefine(recordSet.withTimeToLive(DEFAULT_TTL_IN_SECONDS)));
    }

    PrivateDnsRecordSetImpl defineCnameRecordSet(String name) {
        return setDefaults(prepareInlineDefine(CnameRecordSetImpl.newRecordSet(name, getParent())));
    }

    PrivateDnsRecordSetImpl defineMxRecordSet(String name) {
        return setDefaults(prepareInlineDefine(MxRecordSetImpl.newRecordSet(name, getParent())));
    }

    PrivateDnsRecordSetImpl definePtrRecordSet(String name) {
        return setDefaults(prepareInlineDefine(PtrRecordSetImpl.newRecordSet(name, getParent())));
    }

    PrivateDnsRecordSetImpl defineSoaRecordSet() {
        return setDefaults(prepareInlineDefine(SoaRecordSetImpl.newRecordSet(getParent())));
    }

    PrivateDnsRecordSetImpl defineSrvRecordSet(String name) {
        return setDefaults(prepareInlineDefine(SrvRecordSetImpl.newRecordSet(name, getParent())));
    }

    PrivateDnsRecordSetImpl defineTxtRecordSet(String name) {
        return setDefaults(prepareInlineDefine(TxtRecordSetImpl.newRecordSet(name, getParent())));
    }

    PrivateDnsRecordSetImpl updateAaaaRecordSet(String name) {
        return prepareInlineUpdate(AaaaRecordSetImpl.newRecordSet(name, getParent()));
    }

    PrivateDnsRecordSetImpl updateARecordSet(String name) {
        return prepareInlineUpdate(ARecordSetImpl.newRecordSet(name, getParent()));
    }

    PrivateDnsRecordSetImpl updateCnameRecordSet(String name) {
        return prepareInlineUpdate(CnameRecordSetImpl.newRecordSet(name, getParent()));
    }

    PrivateDnsRecordSetImpl updateMxRecordSet(String name) {
        return prepareInlineUpdate(MxRecordSetImpl.newRecordSet(name, getParent()));
    }

    PrivateDnsRecordSetImpl updatePtrRecordSet(String name) {
        return prepareInlineUpdate(PtrRecordSetImpl.newRecordSet(name, getParent()));
    }

    PrivateDnsRecordSetImpl updateSoaRecordSet() {
        return prepareInlineUpdate(SoaRecordSetImpl.newRecordSet(getParent()));
    }

    PrivateDnsRecordSetImpl updateSrvRecordSet(String name) {
        return prepareInlineUpdate(SrvRecordSetImpl.newRecordSet(name, getParent()));
    }

    PrivateDnsRecordSetImpl updateTxtRecordSet(String name) {
        return prepareInlineUpdate(TxtRecordSetImpl.newRecordSet(name, getParent()));
    }

    void withoutAaaaRecordSet(String name, String etagValue) {
        prepareInlineRemove(AaaaRecordSetImpl.newRecordSet(name, getParent()).withETagOnDelete(etagValue));
    }

    void withoutARecordSet(String name, String etagValue) {
        prepareInlineRemove(ARecordSetImpl.newRecordSet(name, getParent()).withETagOnDelete(etagValue));
    }

    void withoutCnameRecordSet(String name, String etagValue) {
        prepareInlineRemove(CnameRecordSetImpl.newRecordSet(name, getParent()).withETagOnDelete(etagValue));
    }

    void withoutMxRecordSet(String name, String etagValue) {
        prepareInlineRemove(MxRecordSetImpl.newRecordSet(name, getParent()).withETagOnDelete(etagValue));
    }

    void withoutPtrRecordSet(String name, String etagValue) {
        prepareInlineRemove(PtrRecordSetImpl.newRecordSet(name, getParent()).withETagOnDelete(etagValue));
    }

    void withoutSrvRecordSet(String name, String etagValue) {
        prepareInlineRemove(SrvRecordSetImpl.newRecordSet(name, getParent()).withETagOnDelete(etagValue));
    }

    void withoutTxtRecordSet(String name, String etagValue) {
        prepareInlineRemove(TxtRecordSetImpl.newRecordSet(name, getParent()).withETagOnDelete(etagValue));
    }
}
