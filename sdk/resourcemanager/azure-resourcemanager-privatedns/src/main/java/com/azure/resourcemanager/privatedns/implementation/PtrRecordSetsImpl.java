// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.implementation;

import com.azure.resourcemanager.privatedns.fluent.inner.RecordSetInner;
import com.azure.resourcemanager.privatedns.models.PtrRecordSet;
import com.azure.resourcemanager.privatedns.models.PtrRecordSets;
import com.azure.resourcemanager.privatedns.models.RecordType;

/** Implementation of {@link PtrRecordSets}. */
class PtrRecordSetsImpl extends PrivateDnsRecordSetsBaseImpl<PtrRecordSet, PtrRecordSetImpl> implements PtrRecordSets {
    PtrRecordSetsImpl(PrivateDnsZoneImpl privateDnsZone) {
        super(privateDnsZone, RecordType.PTR);
    }

    @Override
    protected PtrRecordSetImpl wrapModel(RecordSetInner inner) {
        if (inner == null) {
            return null;
        }
        return new PtrRecordSetImpl(inner.name(), parent(), inner);
    }
}
