// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/** An immutable client-side representation of a PTR (pointer) record set in Azure Private DNS Zone. */
@Fluent
public interface PtrRecordSet extends PrivateDnsRecordSet {
    /** @return the target domain names of PTR records in this record set */
    List<String> targetDomainNames();
}
