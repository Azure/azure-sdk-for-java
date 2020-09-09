// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.implementation;

import com.azure.resourcemanager.privatedns.fluent.inner.RecordSetInner;
import com.azure.resourcemanager.privatedns.models.ARecordSet;
import com.azure.resourcemanager.privatedns.models.ARecordSets;
import com.azure.resourcemanager.privatedns.models.RecordType;

/** Implementation of {@link ARecordSets}. */
class ARecordSetsImpl extends PrivateDnsRecordSetsBaseImpl<ARecordSet, ARecordSetImpl> implements ARecordSets {

    ARecordSetsImpl(PrivateDnsZoneImpl privateDnsZone) {
        super(privateDnsZone, RecordType.A);
    }

    @Override
    protected ARecordSetImpl wrapModel(RecordSetInner inner) {
        if (inner == null) {
            return null;
        }
        return new ARecordSetImpl(inner.name(), parent(), inner);
    }
}
