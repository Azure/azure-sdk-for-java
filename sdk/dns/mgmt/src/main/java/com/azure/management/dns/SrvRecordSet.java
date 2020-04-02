/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.dns;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * An immutable client-side representation of an SVR (service) record set in Azure DNS Zone.
 */
@Fluent
public interface SrvRecordSet extends DnsRecordSet {
    /**
     * @return the SRV records in this record set
     */
    List<SrvRecord> records();
}
