/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.dns;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * An immutable client-side representation of a AAAA (IPv6) record set in Azure DNS Zone.
 */
@Fluent
public interface AaaaRecordSet extends DnsRecordSet {
    /**
     * @return the IPv6 addresses of AAAA records in this record set
     */
    List<String> ipv6Addresses();
}
