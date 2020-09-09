// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/** An immutable client-side representation of an SVR (service) record set in Azure Private DNS Zone. */
@Fluent
public interface SrvRecordSet extends PrivateDnsRecordSet {
    /** @return the SRV records in this record set */
    List<SrvRecord> records();
}
