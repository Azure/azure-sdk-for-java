// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.implementation;

import com.azure.resourcemanager.privatedns.fluent.models.RecordSetInner;
import com.azure.resourcemanager.privatedns.models.RecordType;
import com.azure.resourcemanager.privatedns.models.TxtRecordSet;
import com.azure.resourcemanager.privatedns.models.TxtRecordSets;

/** Implementation of {@link TxtRecordSets}. */
class TxtRecordSetsImpl extends PrivateDnsRecordSetsBaseImpl<TxtRecordSet, TxtRecordSetImpl> implements TxtRecordSets {
    TxtRecordSetsImpl(PrivateDnsZoneImpl privateDnsZone) {
        super(privateDnsZone, RecordType.TXT);
    }

    @Override
    protected TxtRecordSetImpl wrapModel(RecordSetInner inner) {
        if (inner == null) {
            return null;
        }
        return new TxtRecordSetImpl(inner.name(), parent(), inner);
    }
}
