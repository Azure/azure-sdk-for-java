/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.apigeneration.Fluent;

import java.util.List;

/**
 * An immutable client-side representation of a A (Ipv4) record set in Azure Dns Zone.
 */
@Fluent
public interface ARecordSet extends DnsRecordSet {
    /**
     * @return the Ipv4 addresses of A records in this record set
     */
    List<String> ipv4Addresses();
}
