// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.implementation;

import com.azure.resourcemanager.privatedns.fluent.models.RecordSetInner;
import com.azure.resourcemanager.privatedns.models.CnameRecordSet;
import com.azure.resourcemanager.privatedns.models.CnameRecordSets;
import com.azure.resourcemanager.privatedns.models.RecordType;

/** Implementation of {@link CnameRecordSets}. */
class CnameRecordSetsImpl
    extends PrivateDnsRecordSetsBaseImpl<CnameRecordSet, CnameRecordSetImpl>
    implements CnameRecordSets {

    CnameRecordSetsImpl(PrivateDnsZoneImpl privateDnsZone) {
        super(privateDnsZone, RecordType.CNAME);
    }

    @Override
    protected CnameRecordSetImpl wrapModel(RecordSetInner inner) {
        if (inner == null) {
            return null;
        }
        return new CnameRecordSetImpl(inner.name(), parent(), inner);
    }
}
