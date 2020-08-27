// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.models;

import com.azure.core.annotation.Fluent;
import java.util.List;

/** An immutable client-side representation of an CAA (service) record set in Azure DNS Zone. */
@Fluent
public interface CaaRecordSet extends DnsRecordSet {
    /** @return the CAA records in this record set */
    List<CaaRecord> records();
}
