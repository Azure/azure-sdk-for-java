/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.apigeneration.Fluent;

import java.util.List;

/**
 * An immutable client-side representation of a TXT (text) record set in Azure DNS Zone.
 */
@Fluent
public interface TxtRecordSet extends DnsRecordSet {
    /**
     * @return the TXT records in this record set
     */
    List<TxtRecord> records();
}
