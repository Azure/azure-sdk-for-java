// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/** An immutable client-side representation of a TXT (text) record set in Azure Private DNS Zone. */
@Fluent
public interface TxtRecordSet extends PrivateDnsRecordSet {
    /** @return the TXT records in this record set */
    List<TxtRecord> records();
}
