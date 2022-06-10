// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.dns.models;

import com.azure.core.annotation.Fluent;

/** An immutable client-side representation of a CNAME (canonical name) record set in Azure DNS Zone. */
@Fluent
public interface CnameRecordSet extends DnsRecordSet {
    /** @return the canonical name (without a terminating dot) of CName record in this record set */
    String canonicalName();
}
