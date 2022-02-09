// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.implementation;

import com.azure.resourcemanager.privatedns.fluent.models.RecordSetInner;
import com.azure.resourcemanager.privatedns.models.AaaaRecordSet;
import com.azure.resourcemanager.privatedns.models.AaaaRecordSets;
import com.azure.resourcemanager.privatedns.models.RecordType;

/** Implementation of {@link AaaaRecordSets}. */
class AaaaRecordSetsImpl
    extends PrivateDnsRecordSetsBaseImpl<AaaaRecordSet, AaaaRecordSetImpl>
    implements AaaaRecordSets {

    AaaaRecordSetsImpl(PrivateDnsZoneImpl privateDnsZone) {
        super(privateDnsZone, RecordType.AAAA);
    }

    @Override
    protected AaaaRecordSetImpl wrapModel(RecordSetInner inner) {
        if (inner == null) {
            return null;
        }
        return new AaaaRecordSetImpl(inner.name(), parent(), inner);
    }
}
