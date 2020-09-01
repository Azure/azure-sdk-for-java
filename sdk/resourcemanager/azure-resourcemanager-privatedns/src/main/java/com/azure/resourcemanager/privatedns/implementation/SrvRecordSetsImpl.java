// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.implementation;

import com.azure.resourcemanager.privatedns.fluent.inner.RecordSetInner;
import com.azure.resourcemanager.privatedns.models.RecordType;
import com.azure.resourcemanager.privatedns.models.SrvRecordSet;
import com.azure.resourcemanager.privatedns.models.SrvRecordSets;

/** Implementation of {@link SrvRecordSets}. */
class SrvRecordSetsImpl extends PrivateDnsRecordSetsBaseImpl<SrvRecordSet, SrvRecordSetImpl> implements SrvRecordSets {
    SrvRecordSetsImpl(PrivateDnsZoneImpl privateDnsZone) {
        super(privateDnsZone, RecordType.SRV);
    }

    @Override
    protected SrvRecordSetImpl wrapModel(RecordSetInner inner) {
        if (inner == null) {
            return null;
        }
        return new SrvRecordSetImpl(inner.name(), parent(), inner);
    }
}
