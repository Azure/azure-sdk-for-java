// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.models;

import com.azure.core.annotation.Fluent;
import java.util.List;

/** An immutable client-side representation of a TXT (text) record set in Azure DNS Zone. */
@Fluent
public interface TxtRecordSet extends DnsRecordSet {
    /** @return the TXT records in this record set */
    List<TxtRecord> records();
}
