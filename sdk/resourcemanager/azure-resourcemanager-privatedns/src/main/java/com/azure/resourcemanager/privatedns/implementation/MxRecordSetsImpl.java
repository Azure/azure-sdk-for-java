// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.implementation;

import com.azure.resourcemanager.privatedns.fluent.inner.RecordSetInner;
import com.azure.resourcemanager.privatedns.models.MxRecordSet;
import com.azure.resourcemanager.privatedns.models.MxRecordSets;
import com.azure.resourcemanager.privatedns.models.RecordType;

/** Implementation of {@link MxRecordSets}. */
class MxRecordSetsImpl extends PrivateDnsRecordSetsBaseImpl<MxRecordSet, MxRecordSetImpl> implements MxRecordSets {

    MxRecordSetsImpl(PrivateDnsZoneImpl privateDnsZone) {
        super(privateDnsZone, RecordType.MX);
    }

    @Override
    protected MxRecordSetImpl wrapModel(RecordSetInner inner) {
        if (inner == null) {
            return null;
        }
        return new MxRecordSetImpl(inner.name(), parent(), inner);
    }
}
