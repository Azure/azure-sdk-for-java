// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/** An immutable client-side representation of an MX (mail exchange) record set in an Azure Private DNS Zone. */
@Fluent
public interface MxRecordSet extends PrivateDnsRecordSet {
    /** @return the MX records in this record set */
    List<MxRecord> records();
}
