/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.apigeneration.Fluent;

/**
 * An immutable client-side representation of a CNAME (canonical name) record set in Azure DNS Zone.
 */
@Fluent
public interface CNameRecordSet extends DnsRecordSet {
    /**
     * @return the canonical name (without a terminating dot) of CName record in this record set
     */
    String canonicalName();
}
