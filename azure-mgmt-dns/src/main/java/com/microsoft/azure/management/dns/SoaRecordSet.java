/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.dns;

import com.microsoft.azure.management.apigeneration.Fluent;

/**
 * An immutable client-side representation of a Soa (start of authority) record set in Azure Dns Zone.
 */
@Fluent
public interface SoaRecordSet extends DnsRecordSet {
    /**
     * @return the Soa record in this record set
     */
    SoaRecord record();
}
